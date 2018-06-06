package com.edu.seu.Task;

import com.edu.seu.Configuration.InitConfig;
import com.edu.seu.Protocol.DHT.CompactNodeInfo;
import com.edu.seu.Protocol.DHT.FindNodeQuery;
import com.edu.seu.Protocol.TransactionInfo;
import com.edu.seu.Util.IdUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 初定的策略是：创建一个二十个fix的线程池，每当有收到findnode的请求时就将返回的内容加入到线程池中，
 * 后续的策略是：过滤访问过的nodeid，判定条件是每一个路由表有一个布隆过滤器，同时该布隆过滤器每十五分钟重置一次
 */
@Component
public class FindNodeTask {

    private Charset charset= CharsetUtil.ISO_8859_1;

    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(InitConfig.FINDNODE_TASK_MAXTHREAD);

    private RedisTemplate redisTemplate;

    public FindNodeTask(RedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }


    public void put(byte[] nodeId, byte[] compactInfo, Channel channel){
        fixedThreadPool.execute(new FindNodeRunalbe(nodeId,compactInfo,channel));
    }

    private class FindNodeRunalbe implements Runnable{

        private FindNodeQuery findNodeQuery=null;
        private InetSocketAddress address;
        private Channel channel;

        public FindNodeRunalbe(byte[] nodeId, byte[] compactInfo, Channel channel){
            CompactNodeInfo compactNodeInfo=new CompactNodeInfo(compactInfo);
            this.findNodeQuery=new FindNodeQuery(new String(nodeId,charset), new String(compactNodeInfo.getNodeId(),charset));
            this.address=compactNodeInfo.toAddress();
            this.channel=channel;
        }

        @Override
        public void run() {

            redisTemplate.opsForValue().set(findNodeQuery.getTid(),new TransactionInfo(findNodeQuery.getMethod(),findNodeQuery.getTid()));
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(InitConfig.bEncoding.encodingObject(findNodeQuery.toMap()).getBytes(charset)), address));

        }
    }

}
