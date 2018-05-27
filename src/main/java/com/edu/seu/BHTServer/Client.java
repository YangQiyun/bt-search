package com.edu.seu.BHTServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Slf4j
public class Client {

    public static void main(String [] args){
        NioEventLoopGroup group=new NioEventLoopGroup();
        try{
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST,true)
                    .handler(new clientHandler());
            String content=new String("send something from client");
           ChannelFuture future= bootstrap.connect(new InetSocketAddress("127.0.0.1",8000)).sync();
           log.info(future.isSuccess()?"success":"fail");
           future.channel().writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(content.getBytes()),new InetSocketAddress("127.0.0.1",8000))).addListener(new clientListener());
           //由于是立即返回所以是fail log.info(future.isSuccess()?"write success":"write fail");
            future.channel().closeFuture().sync();
            log.info("client close");
        }catch (InterruptedException e){
            log.error("error :"+e.getMessage());
        }
        finally {
            group.shutdownGracefully();
        }
    }

    public static class clientHandler extends SimpleChannelInboundHandler<DatagramPacket>{

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error(cause.getMessage());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("client active");
        }

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.info("channelRead");
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.info("channelReadComplete");
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            super.flush(ctx);
            log.info("flush is finished");
            ctx.channel().close();
        }


    }

    public static class clientListener implements ChannelFutureListener{

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if(future.isVoid()){
                log.info("this is a void future");
            }
            if(future.isCancelled()){
                log.info("this is a cancel future");
            }
            if(future.isDone()){
                log.info("this is a done future");

            }
            if(future.isSuccess()){
                log.info("this is a success future");
            }
            //log.info("this is the cause "+future.cause().getMessage());
            future.channel().close();
        }


    }
}
