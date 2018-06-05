package com.edu.seu.Protocol.DHT;

import com.edu.seu.Configuration.InitConfig;
import com.edu.seu.Exception.BtException;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Protocol.KRPC.Responses;
import com.edu.seu.Protocol.RoutingTable;
import com.edu.seu.Util.ConvertUtil;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * Get peers关联着一个torrent infohash。"q"="get_peers"。
 * 一个get_peers的请求有俩个参数，"id"表示请求node的ID，"info_hash"表示torrent的infohash值。
 * 如果被请求的node有关于infohash的peers节点信息，那么这些信息将会组成一个list包含在"values"的键对中。
 * Each string containing "compact" format peer information for a single peer.
 * 如果被请求节点没有相关的peers信息，那么将会回复"nodes"键对，其值是一个它的路由表中离infohash理论上最近的8个节点的node的"compact node info"紧排一起的200字节二进制串。
 * 另一方面，"token"键值对需要包含在返回字典的。这个值将会在未来可能的announce_peer中被使用到。其值应该要比较短的的二进制串。
 *
 * arguments: {"id" : "<querying nodes id>", "info_hash" : "<20-byte infohash of target torrent>"}
 * response: {"id" : "<queried nodes id>", "token" :"<opaque write token>", "values" : ["<peer 1 info string>", "<peer 2 info string>"]}
 * or: {"id" : "<queried nodes id>", "token" :"<opaque write token>", "nodes" : "<compact node info>"}
 *
 * get_peers Query = {"t":"aa", "y":"q", "q":"get_peers", "a": {"id":"abcdefghij0123456789", "info_hash":"mnopqrstuvwxyz123456"}}
 * Response with peers = {"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "values": ["axje.u", "idhtnm"]}}
 * Response with closest nodes = {"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "nodes": "def456..."}}
 * */
public class GetPeersResponse extends Responses implements DHT{

    public GetPeersResponse(String id,String token,List<String> values){
        setID(id);
        setValues(values);
        setToken(token);
    }

    public GetPeersResponse(String tid,String id,String nodeid,RoutingTable table, byte[] mCompactId){
        setTid(tid);
        setID(id);
        setToken(InitConfig.token);
        setCompactNode(nodeid,table,mCompactId);
    }

    public GetPeersResponse(String id,String token,String nodes){
        setID(id);
        setToken(token);
        setCompactNode(nodes);
    }


    public List<String> getValues(){
        Map<String,Object> args=getArgs();
        if(args==null)
            return null;
        return (List<String> ) args.get("values");
    }

    public void setValues(List<String> values){
        Map<String,Object> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("values",values);
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

    public String getCompactNode(){
        Map<String,Object> args=getArgs();
        if(args==null)
            return null;
        return (String) args.get("nodes");
    }


    /**
     * 通过nodeid找到路由表中的近八个节点，同时强行替换其中的一个节点为本节点
     * 如果找不到八个节点则抛出异常
     */
    public void setCompactNode(String compactNode, RoutingTable table, byte[] mCompactId){
        List<byte[]> get=table.get(compactNode.getBytes(CharsetUtil.ISO_8859_1));

        get.set(0,mCompactId);

        if(get==null)
            throw new BtException(BtException.ERROR_CODE.FINDNODE_LEAK,"GetPeersResponse 路由表中不够八个节点");
        StringBuilder content=new StringBuilder();
        for(byte[] item:get){
            content.append(new String(item,CharsetUtil.ISO_8859_1));
        }
        Map<String,String> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("nodes",content.toString());
        setArgs(args);

    }


    public void setCompactNode(String nodes){
        Map<String,Object> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("nodes",nodes);
        setArgs(args);
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


    public static Responses decodeArgs(Map<String, Bencoding.btDecodeResult> args) {
        //格式初步确认
        if(args.size()==3&&args.containsKey("id")&&args.containsKey("token")
                &&(args.containsKey("values")||args.containsKey("nodes"))){
            //参数类型确认
            if(args.get("id").type==Bencoding.beType.ByteString
                    &&args.get("token").type==Bencoding.beType.ByteString){
                if(args.containsKey("values")&&args.get("values").type==Bencoding.beType.List)
                    return new GetPeersResponse((String) args.get("id").value,(String) args.get("token").value, (List<String>) args.get("values").value);
                if(args.containsKey("nodes")&&args.get("nodes").type==Bencoding.beType.ByteString)
                    return new GetPeersResponse((String) args.get("id").value,(String) args.get("token").value, (String) args.get("nodes").value);
            }
        }
        throw new BtException("GetPeersResponse格式出错");
    }
}
