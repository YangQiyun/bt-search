package com.edu.seu.Protocol.DHT;

import com.edu.seu.Exception.BtException;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Protocol.KRPC.Responses;

import java.util.Map;
import java.util.TreeMap;

/*
*
* Announce that the peer, controlling the querying node, is downloading a torrent on a port.
* 该请求有四个参数，"id"代表请求node的ID，"info_hash"代表torrent的infohash值，"port"代表对应的端口，"token"代表前一个发送的get_peers请求返回的token值。
* 该请求的必须是发向提供该token的IP地址的节点。
* 接受方需存储请求方的ip地址和更新存储该infohash的peer关联信息的端口信息。
* 这里有一个可选的参数implied_port，它的值可以是0或1.当它是非0,那么该端口参数应该被无视将源UDP包的端口地址替代该peer的端口值。这对于peer处于NAT网络下，未能知道其实际的端口值有一定意义。同时支持UTP，它们接受来自相同的端口的信息作为DHT端口。
*
* arguments: {"id" : "<querying nodes id>",
* "implied_port": <0 or 1>,
* "info_hash" : "<20-byte infohash of target torrent>",
* "port" : <port number>,
* "token" : "<opaque token>"}
*
* response: {"id" : "<queried nodes id>"}
*
*Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
* */

public class AnnouncePeerResponse extends Responses implements DHT {

    public AnnouncePeerResponse(String tid,String id){
        setTid(tid);
        setID(id);
    }

    public AnnouncePeerResponse(String id){
        setID(id);
    }

    @Override
    public String getID() {
        Map<String,String> args=getArgs();
        if(args==null)
            return null;
        return args.get("id");
    }

    @Override
    public void setID(String id) {
        Map<String,String> args=getArgs();
        if(args==null)
            args=new TreeMap<String, String>();
        args.put("id",id);
        setArgs(args);
    }


    public static Responses decodeArgs(Map<String, Bencoding.btDecodeResult> args) {
        //格式初步确认
        if(args.size()==1&&args.containsKey("id")){
            //参数类型确认
            if(args.get("id").type==Bencoding.beType.ByteString){
                return new AnnouncePeerResponse((String) args.get("id").value);
            }
        }
        throw new BtException("AnnouncePeerResponse格式出错");
    }
}
