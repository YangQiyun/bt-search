package com.edu.seu.BHTServer;

import com.edu.seu.Configuration.InitConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/*
*
* */
@Slf4j
@Component
public class DhtServer {

    //server 监听的端口
    private int port;

    public DhtServer(InitConfig config){
        this.port=config.port;
    }

    public void init() throws InterruptedException {
        NioEventLoopGroup ServerGroup=new NioEventLoopGroup();
        try{
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(ServerGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST,true)//广播
                    .handler(new DhtServerHandler());
            //阻塞绑定然后进行closefuture等待
            bootstrap.bind(port).sync().channel().closeFuture().await();
            log.info("server is close");
        }finally {
            if(ServerGroup!=null)
                ServerGroup.shutdownGracefully();
        }
    }


}
