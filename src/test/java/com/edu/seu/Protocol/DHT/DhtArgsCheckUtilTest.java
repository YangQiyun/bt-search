package com.edu.seu.Protocol.DHT;

import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Util.ConvertUtil;
import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCYEnum;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static com.edu.seu.Util.ConvertUtil.HexString2Byte;

public class DhtArgsCheckUtilTest {

    public byte[] findnode(){
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
        return what.getBytes(CharsetUtil.ISO_8859_1);
    }

    @Test
    public void decodeAndCheckResp() {

    }

    @Test
    public void parseT() {
        Assert.assertEquals("aa",DhtArgsCheckUtil.parseT(DhtArgsCheckUtil.decodeAndCheckResp(findnode())));
    }

    @Test
    public void parseY() {
        Assert.assertEquals(KRPCYEnum.QUERY,DhtArgsCheckUtil.parseY(DhtArgsCheckUtil.decodeAndCheckResp(findnode())));
    }

    @Test
    public void parseQ() {
        Assert.assertEquals(DHTMethodQvalue.FINDNODE,DhtArgsCheckUtil.parseQ(DhtArgsCheckUtil.decodeAndCheckResp(findnode())));
    }

    @Test
    public void parseA() {
        String target="1619ecc9373c3639f4ee3e261638f29b33a6cbd6";
        byte[] result=HexString2Byte(target);
        String temp=new String(result, CharsetUtil.ISO_8859_1);
       FindNodeQuery findNodeQuery= (FindNodeQuery) DhtArgsCheckUtil.parseA(DhtArgsCheckUtil.decodeAndCheckResp(findnode()),DhtArgsCheckUtil.parseQ(DhtArgsCheckUtil.decodeAndCheckResp(findnode())));
       Assert.assertEquals(temp,findNodeQuery.getID());
       Assert.assertEquals(temp,findNodeQuery.getTarget());
    }

    @Test
    public void parseR() {
    }
}