package com.edu.seu.BHTServer;


import com.edu.seu.Configuration.InitConfig;
import com.edu.seu.Exception.BtException;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Protocol.DHT.*;
import com.edu.seu.Protocol.KRPC.Errors;
import com.edu.seu.Protocol.KRPC.Responses;
import com.edu.seu.Protocol.RoutingTable;
import com.edu.seu.Protocol.TransactionInfo;
import com.edu.seu.Util.ConvertUtil;
import com.edu.seu.Util.IdUtil;
import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCYEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

import static com.edu.seu.Exception.BtException.ERROR_CODE.FINDNODE_NO_NODES;
import static com.edu.seu.Protocol.DHT.DhtArgsCheckUtil.*;
import static com.edu.seu.Util.ConvertUtil.HexString2Byte;


@ChannelHandler.Sharable
@Slf4j
public class DhtServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{


    //处理时机在发送请求的时候缓存，同时获取到信息后就清除
    private RedisTemplate redisTemplate;
    private Charset charset=CharsetUtil.ISO_8859_1;
    private String nodeId;
    private RoutingTable routingTable;
    private final String className=this.getClass().getName();
    private InetSocketAddress address;
    private byte[] mCompactNodeid;
    private byte[] mBitNodeid;
    private InitConfig config;

    public DhtServerHandler(RedisTemplate redisTemplate,byte[] nodeId,InetSocketAddress address,InitConfig config){
        this.redisTemplate=redisTemplate;
        this.nodeId=new String(nodeId,charset);
        routingTable=new RoutingTable(new CompactNodeInfo(nodeId,address).CompactNodeInfotoBytes());
        this.address=address;
        mCompactNodeid=ConvertUtil.nodeAndaddress(nodeId,address);
        mBitNodeid=nodeId;
        this.config=config;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("messageReceived.exceptionCaught");
        StackTraceElement[] stackTraceElements=cause.getStackTrace();
        for (StackTraceElement element:stackTraceElements){
            log.error(element.toString());
        }
        ctx.close();
    }

    /*
    * 当通道打开，随机节点进行搜寻入驻到DHT网络中去
    * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("DhtServerHandler.channelActive");

        final InetSocketAddress[] initAddressArray =config.getInitAddressArray();
        for (InetSocketAddress address : initAddressArray) {
            FindNodeQuery findNodeQuery=new FindNodeQuery(nodeId, new String(IdUtil.generateNodeId(),charset));
            redisTemplate.opsForValue().set(findNodeQuery.getTid(),new TransactionInfo(findNodeQuery.getMethod(),findNodeQuery.getTid()));
            ctx.channel().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(InitConfig.bEncoding.encodingObject(findNodeQuery.toMap()).getBytes(charset)), address));
        }

    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

        //log.info("DhtServerHandler.messageReceived");
        ByteBuf byteBuf = msg.content();
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);

        try {
            //进行解析
            Map<String, Bencoding.btDecodeResult> btMap=decodeAndCheckResp(result);
            //1.获取tid信息标识
            String tid=parseT(btMap);
            //2.获取KRPC请求方式,根据请求方式进行不同处理
            KRPCYEnum Ymethod=parseY(btMap);
            switch (Ymethod){
                case QUERY:
                    //2.1 如果是DHT请求,根据请求方式进行不同处理
                    DHTMethodQvalue Qmethod=parseQ(btMap);
                    Responses response=null;
                    switch (Qmethod){
                        //2.1.1 PING请求，直接回应
                        case PING:
                            //log.info("收到PING请求");
                            PingQuery pingQuery= (PingQuery) parseA(btMap,Qmethod);
                            response=new PingResponse(pingQuery.getTid(),nodeId);
                            break;
                        //2.1.2 FINDNODE请求，得到的路由表信息返回值不超过八个直接就丢弃该请求
                        case FINDNODE:
                            //log.info("收到FINDNODE请求");
                            FindNodeQuery findNodeQuery = (FindNodeQuery) parseA(btMap, Qmethod);
                            String targetCompactNode=new String(ConvertUtil.nodeAndaddress(findNodeQuery.getTarget().getBytes(charset),msg.sender()),charset);
                            response = new FindNodeResponse(findNodeQuery.getTid(), nodeId,targetCompactNode, routingTable,mCompactNodeid);
                            break;
                        //2.1.3 GETPEERS请求
                        case GETPEERS:
                            //log.info("收到GETPEERS请求");
                            GetPeersQuery getPeersQuery= (GetPeersQuery) parseA(btMap,Qmethod);
                            String infohashCompactNode=new String(ConvertUtil.nodeAndaddress(getPeersQuery.getInfoHash().getBytes(charset),msg.sender()),charset);
                            response=new GetPeersResponse(getPeersQuery.getTid(),nodeId,infohashCompactNode,routingTable,mCompactNodeid);
                            break;
                        //2.1.4 ANNOUNCEPEER请求，对内容进行存储
                        case ANNOUNCEPEER:
                            AnnouncePeerQuery announcePeerQuery= (AnnouncePeerQuery) parseA(btMap,Qmethod);
                            log.info("收到ANNOUNCEPEER"+announcePeerQuery.getInfoHash()+" port: "+announcePeerQuery.getPort()+" implid_port:"+announcePeerQuery.getImpliedPort());
                            response=new AnnouncePeerResponse(announcePeerQuery.getTid(),nodeId);
                            break;
                    }
                    //2.2 统一对处理的结果进行回复
                    ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(InitConfig.bEncoding.encodingObject(response.toMap()).getBytes(charset)), msg.sender()));
                    break;
                case RESPONSE:
                    //2.3 如果是DHT的应答
                    TransactionInfo transactionInfo= (TransactionInfo) redisTemplate.opsForValue().get(tid);
                    redisTemplate.delete(tid);
                    //没有记录则直接丢弃
                    if(transactionInfo==null)
                        return;
                    switch (transactionInfo.getDhtMethodQvalue()){
                        //2.3.1 对于PING的应答，更新路由表
                        case PING:
                            log.info("收到PING答复");
                            PingResponse pingResponse= (PingResponse) parseR(btMap,transactionInfo.getDhtMethodQvalue());
                            routingTable.put(ConvertUtil.nodeAndaddress(pingResponse.getID().getBytes(charset),msg.sender()));
                            break;
                        //2.3.2 对于FINDNODE的应答，更新路由表
                        case FINDNODE:
                            //log.info("收到FINDNODE答复");
                            FindNodeResponse findNodeResponse=(FindNodeResponse) parseR(btMap,transactionInfo.getDhtMethodQvalue());
                            ByteBuf resultBuf=Unpooled.wrappedBuffer(findNodeResponse.getCompactNode().getBytes(charset));
                            //这个byte[] compactInfo放外面会导致所有值都一样= =
                            for(;;){
                                if(resultBuf.readableBytes()<26)
                                break;
                                byte[] compactInfo=new byte[26];
                                resultBuf.readBytes(compactInfo);
                                routingTable.put(compactInfo);
                                config.findNodeTask.put(mBitNodeid,compactInfo,ctx.channel());
                            }
                            break;
                        case GETPEERS:
                            log.info("收到GETPEERS答复");
                            break;
                        case ANNOUNCEPEER:
                            log.info("收到ANNOUNCEPEER答复");
                            break;
                    }
                    break;
                case ERROR:
                    Errors error=parseE(btMap);
                    //log.info("收到KRPC的ERROR答复,错误码{},错误描述{}",error.getError().getCode(),error.getReceivedContent());
                    break;
                case UNKNOWN:
                    break;
            }
        }catch (BtException e){
            if(e.getError_code()==BtException.ERROR_CODE.FINDNODE_LEAK||e.getError_code()==FINDNODE_NO_NODES)
                return;
            else if(e.getError_code()==BtException.ERROR_CODE.PARSET_ERROR||e.getError_code()==BtException.ERROR_CODE.FORMAT_ERROR){
                //log.error(new String(result,charset));
            }
            else
                log.error(e.getMessage());
            return;
        }catch (Exception e){
            StackTraceElement[] stackTraceElements=e.getStackTrace();
            for (StackTraceElement element:stackTraceElements){
                log.error(element.toString());
            }
            return;
        }

    }




}
