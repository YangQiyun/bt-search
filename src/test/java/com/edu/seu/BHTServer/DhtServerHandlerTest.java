package com.edu.seu.BHTServer;

import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Util.ConvertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;

import static com.edu.seu.Util.ConvertUtil.HexString2Byte;
import static org.junit.Assert.*;

public class DhtServerHandlerTest {

    @Test
    public void channelActive() {
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
        ByteBuf buf= Unpooled.wrappedBuffer(what.getBytes(CharsetUtil.ISO_8859_1));
        //ctx.channel().writeAndFlush(new DatagramPacket(buf,new InetSocketAddress("dht.transmissionbt.com", 6881)));
        //log.info("channel active success");
    }
}