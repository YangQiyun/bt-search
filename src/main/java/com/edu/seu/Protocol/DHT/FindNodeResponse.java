package com.edu.seu.Protocol.DHT;

import com.edu.seu.Exception.BtException;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Protocol.KRPC.Responses;

import java.util.Map;
import java.util.TreeMap;

/*
 * DHT协议中的find_node请求
 *
 * Find node是用于寻找给定ID的node节点的相关联系信息。
 * "q"=”find_node"一个find_node的请求有两个参数，
 * "id"表示请求node的ID，"target"表示待查询的node的ID。
 * 当一个node接收到find_node请求，它应该回复"nodes"键对，
 * 其值是一个它的路由表中离target ID理论上最近的8个节点的node的"compact node info"紧排一起的200字节二进制串。
 *
 * arguments: {"id" : "<querying nodes id>", "target" : "<id of target node>"}
 * response: {"id" : "<queried nodes id>", "nodes" : "<compact node info>"}
 *
 * Response = {"t":"aa", "y":"r", "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
 * */
public class FindNodeResponse extends Responses implements DHT{

    public FindNodeResponse(String id,String nodes){
        setID(id);
        setNodes(nodes);
    }

    public void setNodes(String nodes){
        Map<String,String> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("nodes",nodes);
        setArgs(args);

    }

    public String getNodes(){
        Map<String,String> args=getArgs();
        if(args==null)
            return null;
        return args.get("nodes");
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
        if(args.size()==2&&args.containsKey("id")&&args.containsKey("nodes")){
            //参数类型确认
            if(args.get("id").type==Bencoding.beType.ByteString
                    &&args.get("nodes").type==Bencoding.beType.ByteString){
                return new FindNodeResponse((String) args.get("id").value,(String) args.get("nodes").value);
            }
        }
        throw new BtException("FindNodeResponse格式出错");
    }
}
