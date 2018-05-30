package com.edu.seu.BHTServer;


import com.edu.seu.Configuration.InitConfig;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Util.ConvertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;

import static com.edu.seu.Util.ConvertUtil.HexString2Byte;

@ChannelHandler.Sharable
@Slf4j
public class DhtServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("messageReceived.exceptionCaught");
        ctx.close();
    }

    /*
    * 当通道打开，记录下我们的服务器通道
    * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("DhtServerHandler.channelActive");
        Bencoding bEncoding=new Bencoding();
        String target="1619ecc9373c3639f4ee3e261638f29b33a6cbd6";
        byte[] result=HexString2Byte(target);
        String temp=new String(result, CharsetUtil.ISO_8859_1);

        byte[] bytes1="d1:ad2:id20:".getBytes();
        byte[] bytes2= ConvertUtil.getNode();
        byte[] bytes3=":target20:".getBytes();
        byte[] byte4=ConvertUtil.getNode();
        byte[] bytes5="e1:q9:find_node1:t2:aa1:y1:qe".getBytes();
        Map<String,Bencoding.btDecodeResult> map=new TreeMap<>();
        map.put("t",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,"aa"));
        map.put("y",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,"q"));
        map.put("q",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,"find_node"));
        Map<String,Bencoding.btDecodeResult> small=new TreeMap<>();
        small.put("id",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,temp));
        small.put("target",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,temp));
        map.put("a",new Bencoding.btDecodeResult(Bencoding.beType.Dictionary,small));
        String what=bEncoding.encodingObject(map);
        //ByteBuf buf=Unpooled.wrappedBuffer(bytes1,bytes2,bytes3,byte4,bytes5);
        ByteBuf buf=Unpooled.wrappedBuffer(what.getBytes(CharsetUtil.ISO_8859_1));
        ctx.channel().writeAndFlush(new DatagramPacket(buf,new InetSocketAddress("dht.transmissionbt.com", 6881)));
        log.info("channel active success");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        log.info("DhtServerHandler.messageReceived");
        byte[] result=getBytes(msg);

        //ctx.writeAndFlush(getBytes(msg));
    }

    private byte[] getBytes(DatagramPacket packet) {
        //读取消息到byte[]
        ByteBuf byteBuf = packet.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        log.info("{}-收到消息,发送者:{},未解码消息内容:{}", packet.sender(), new String(bytes, CharsetUtil.ISO_8859_1));
        Bencoding.btDecodeResult a=InitConfig.bEncoding.decodingObject(bytes,0);
        return bytes;
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx,msg);
        log.info("channelRead");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        log.info("channelReadComplete");
    }

}
