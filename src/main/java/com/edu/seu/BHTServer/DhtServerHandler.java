package com.edu.seu.BHTServer;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

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

    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        log.info("DhtServerHandler.messageReceived");

        ctx.writeAndFlush(getBytes(msg));
    }

    private byte[] getBytes(DatagramPacket packet) {
        //读取消息到byte[]
        ByteBuf byteBuf = packet.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        log.info("{}1-收到消息,发送者:{},未解码消息内容:{}", packet.sender(), new String(bytes, CharsetUtil.UTF_8));
        return bytes;
    }


    //用这两个messagereceived就没用了
    /*
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channelRead");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("channelReadComplete");
    }
    */
}
