package com.edu.seu.Protocol.DHT;

import com.edu.seu.Exception.BtException;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Protocol.KRPC.Queries;
import com.edu.seu.enums.DHTMethodQvalue;

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
* announce_peers Query = {"t":"aa", "y":"q", "q":"announce_peer", "a": {"id":"abcdefghij0123456789", "implied_port": 1, "info_hash":"mnopqrstuvwxyz123456", "port": 6881, "token": "aoeusnth"}}
* */
public class AnnouncePeerQuery extends Queries implements DHT{

    public AnnouncePeerQuery(){
        setMethod(DHTMethodQvalue.ANNOUNCEPEER);
    }

    public AnnouncePeerQuery(String id,int implied_port,int port,String token,String info_hash){
        setID(id);
        setPort(port);
        setImpliedPort(implied_port);
        setToken(token);
        setInfoHash(info_hash);
    }


    public AnnouncePeerQuery(String id,int port,String token,String info_hash){
        setID(id);
        setPort(port);
        setToken(token);
        setInfoHash(info_hash);
    }


    public static Queries decodeArgs(Map<String, Bencoding.btDecodeResult> args) {
        //格式初步确认
        if(args.size()>=4&&args.containsKey("id")&&args.containsKey("info_hash")&&args.containsKey("token")&&args.containsKey("port")){
            //参数类型确认
            if(args.get("id").type==Bencoding.beType.ByteString
                    && args.get("info_hash").type==Bencoding.beType.ByteString
                    && args.get("token").type==Bencoding.beType.ByteString
                    && args.get("port").type==Bencoding.beType.Integer){
                AnnouncePeerQuery result=new AnnouncePeerQuery((String) args.get("id").value,(Integer) args.get("port").value,(String) args.get("token").value,(String) args.get("info_hash").value);
                if(args.containsKey("implied_port")){
                    if(args.get("implied_port").type==Bencoding.beType.Integer) {
                        result.setImpliedPort((Integer) args.get("implied_port").value);
                        return result;
                    }
                }
            }
        }
        throw new BtException("AnnouncePeerQuery格式出错");
    }


    @Override
    public String getID() {
        Map<String,Object> args=getArgs();
        if(args==null)
            return null;
        return (String) args.get("id");
    }

    @Override
    public void setID(String id) {
        Map<String,Object> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("id",id);
        setArgs(args);
    }


    public void setImpliedPort(int implied_port) {
        Map<String,Object> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("implied_port",implied_port);
        setArgs(args);
    }


    public int getImpliedPort() {
        Map<String,Object> args=getArgs();
        if(args==null)
            return 0;
        return (int) args.get("implied_port");
    }


    public void setInfoHash(String infoHash) {
        Map<String,Object> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("info_hash",infoHash);
        setArgs(args);
    }

    public String getInfoHash() {
        Map<String,Object> args=getArgs();
        if(args==null)
            return null;
        return (String) args.get("info_hash");
    }

    public int getPort(){
        Map<String,Object> args=getArgs();
        if(args==null)
            return -1;
        return (int) args.get("port");
    }

    public void setPort(int port){
        Map<String,Object> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("port",port);
        setArgs(args);
    }

    public String getToken(){
        Map<String,Object> args=getArgs();
        if(args==null)
            return null;
        return (String) args.get("token");
    }

    public void setToken(String token){
        Map<String,Object> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("token",token);
        setArgs(args);
    }



}
