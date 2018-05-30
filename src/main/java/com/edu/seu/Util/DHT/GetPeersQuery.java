package com.edu.seu.Util.DHT;

import com.edu.seu.Exception.BtException;
import com.edu.seu.Util.Bencode.BencodeSupport;
import com.edu.seu.Util.Bencode.Bencoding;
import com.edu.seu.Util.KRPC.Queries;
import com.edu.seu.enums.DHTMethodQvalue;

import java.util.Map;
import java.util.TreeMap;

/*
*
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
public class GetPeersQuery extends Queries implements DHT{

    public GetPeersQuery(){
        setMethod(DHTMethodQvalue.GETPEERS);
    }

    public GetPeersQuery(String id,String info_hash){
        this();
        setID(id);
        setInfoHash(info_hash);
    }

    public void setInfoHash(String infoHash) {
        Map<String,String> args=getArgs();
        if(args==null)
            args=new TreeMap<>();
        args.put("info_hash",infoHash);
        setArgs(args);
    }

    public String getInfoHash() {
        Map<String,String> args=getArgs();
        if(args==null)
            return null;
        return args.get("info_hash");
    }


    @Override
    public void setID(String id) {
        Map<String,String> args=getArgs();
        if(args==null)
            args=new TreeMap<String, String>();
        args.put("id",id);
        setArgs(args);
    }

    @Override
    public String getID() {
        Map<String,String> args=getArgs();
        if(args==null)
            return null;
        return args.get("id");
    }


    public static Queries decodeArgs(Map<String, Bencoding.btDecodeResult> args) {
        //格式初步确认
        if(args.size()==2&&args.containsKey("id")&&args.containsKey("info_hash")){
            //参数类型确认
            if(args.get("id").type==Bencoding.beType.ByteString
                    && args.get("info_hash").type==Bencoding.beType.ByteString){
                return new GetPeersQuery((String) args.get("id").value,(String) args.get("info_hash").value);
            }
        }
        throw new BtException("GetPeersQuery格式出错");
    }
}
