package com.edu.seu.Util.DHT;

import com.edu.seu.Exception.BtException;
import com.edu.seu.Util.Bencode.BencodeSupport;
import com.edu.seu.Util.Bencode.Bencoding;
import com.edu.seu.Util.KRPC.Responses;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * DHT协议中的ping请求
 *
 * 最基础的请求就是ping。"q"="ping"一个ping请求有一个单一的参数，
 * "id"的值是一个表示发送请求的node的ID的20字节长的二进制字符串网络字节序。
 * 完整合适的应答ping也需要一个单一的键"id"包含了应答node的node ID序列号。
 *
 * Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
 * */
public class PingResponse extends Responses implements DHT {

    public PingResponse(String id) {
        setID(id);
    }


    @Override
    public String getID() {
        Map<String, String> args = getArgs();
        if (args == null)
            return null;
        return args.get("id");
    }

    @Override
    public void setID(String id) {
        Map<String, String> args = getArgs();
        if (args == null)
            args = new TreeMap<>();
        args.put("id", id);
        setArgs(args);
    }


    public static Responses decodeArgs(Map<String, Bencoding.btDecodeResult> args) {
        //格式初步确认
        if (args.size() == 1 && args.containsKey("id")) {
            //参数类型确认
            if (args.get("id").type == Bencoding.beType.ByteString) {
                return new PingResponse((String) args.get("id").value);
            }
        }
        throw new BtException("PingResponse格式出错");
    }
}
