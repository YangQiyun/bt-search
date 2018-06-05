package com.edu.seu.BHTServer;

import com.edu.seu.Configuration.InitConfig;
import com.edu.seu.Util.IdUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


/*
*
* */
@Slf4j
@Component
public class DhtServer {

    @Autowired(required = true)
    private RedisTemplate redisTemplate;

    private InitConfig config;

    private List<DhtServerHandler> dhtServerHandlers;

    public DhtServer(InitConfig config){
        this.config=config;
    }


    public void init() {
        List<Integer> ports = config.getPorts();
        dhtServerHandlers=new ArrayList<>(ports.size());
        for (int i = 0; i < ports.size(); i++) {
            final int index = i;
            dhtServerHandlers.add(i,new DhtServerHandler(redisTemplate,IdUtil.generateNodeId(),new InetSocketAddress("223.3.175.181",ports.get(index))));
            new Thread(()->run(ports.get(index),index)).start();
        }

        //等待连接成功,获取到发送用的channel,再进行下一步
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 保证UDP服务端开启,即使运行出错
     */
    private void run(int port,int index) {
        //while (true){
            try {
                run1(port,index);
            } catch (Exception e) {
                log.error("服务器端口:{},发生未知异常,准备重新启动.异常:{}",port,e.getMessage(),e);
            }
       // }
    }

    /**
     * 启动UDP服务端,监听
     */
    private void run1(int port,int index) throws Exception {
        log.info("服务端启动...当前端口:{}",port);
        EventLoopGroup eventLoopGroup = null;
        try {
            //创建线程组 - 手动设置线程数,默认为cpu核心数2倍
            eventLoopGroup =  new NioEventLoopGroup(5);
            //创建引导程序
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioDatagramChannel.class)//通道类型也为UDP
                    .option(ChannelOption.SO_BROADCAST, true)//是广播,也就是UDP连接
                    .option(ChannelOption.SO_RCVBUF, 10000 * 1024)// 设置UDP读缓冲区为3M
                    .option(ChannelOption.SO_SNDBUF, 10000 * 1024)// 设置UDP写缓冲区为3M
                    .handler(dhtServerHandlers.get(index));//配置的业务处理类
            bootstrap.bind(port).sync().channel().closeFuture().await();
        }finally {
            if(eventLoopGroup != null)
                eventLoopGroup.shutdownGracefully();
        }
    }


}
