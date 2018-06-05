package com.edu.seu.Protocol.DHT;

import com.edu.seu.Exception.BtException;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Protocol.KRPC.Queries;
import com.edu.seu.enums.DHTMethodQvalue;

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
 * find_node Query = {"t":"aa", "y":"q", "q":"find_node", "a": {"id":"abcdefghij0123456789", "target":"mnopqrstuvwxyz123456"}}
 * */
public class FindNodeQuery extends Queries implements DHT{

    public FindNodeQuery(){
        setMethod(DHTMethodQvalue.FINDNODE);
    }

    public FindNodeQuery(String id,String target){
        this();
        setID(id);
        setTarget(target);
    }

    public String getTarget(){
        Map<String,String> args=getArgs();
        if(args==null)
            return null;
        return args.get("target");
    }

    public void setTarget(String target){
        Map<String,String> args=getArgs();
        if(args==null)
            args=new TreeMap<String, String>();
        args.put("target",target);
        setArgs(args);
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


    public static Queries decodeArgs(Map<String, Bencoding.btDecodeResult> args) {
        //格式初步确认
        if(args.size()==2&&args.containsKey("id")&&args.containsKey("target")){
            //参数类型确认
            if(args.get("id").type==Bencoding.beType.ByteString
                    && args.get("target").type==Bencoding.beType.ByteString){
                return new FindNodeQuery((String) args.get("id").value,(String) args.get("target").value);
            }
        }
        throw new BtException("FindNodeQuery格式出错");
    }

    /*
    *
    @Override
    public void decodingByte(byte[] response, int start) {
        //进行解码获取内容
        Bencoding.btDecodeResult result= InitConfig.bEncoding.decodingObject(response,start);
        //解码结果类型判断
        if(result.type!= Bencoding.beType.Dictionary)
            throw new BtException(className+" - FindNode decodingByte: 解析字节序出错非字典结构");
        //对解码结果进行分派信息录入
        Map<String,Bencoding.btDecodeResult> resultMap= (Map<String, Bencoding.btDecodeResult>) result;
        for(Map.Entry<String,Bencoding.btDecodeResult> entry:resultMap.entrySet()){
            if(entry.getKey().equals("t")){
                setT((String) entry.getValue().value);

                continue;
            }
            if(entry.getKey().equals("y")){
                if(entry.getValue().type== Bencoding.beType.ByteString){
                    if(((String)entry.getValue().value).equals("r"))
                        setY(yType.RESPONSE);
                    else
                        throw new BtException(className+" - FindNode decodingByte: 'y'对应值非'r'");
                }else
                    throw new BtException(className+" - FindNode decodingByte: 'y'对应值非bytestring");
                continue;
            }
            if(entry.getKey().equals("r")){
                if(entry.getValue().type== Bencoding.beType.Dictionary){
                    Map<String,Bencoding.btDecodeResult> rMap= (Map<String, Bencoding.btDecodeResult>) entry.getValue().value;
                    if(rMap.size()!=2)
                        throw new BtException(className+" - FindNode decodingByte: 'r'对应值参数个数不等于两个");
                    try {
                        setId((String) rMap.get("id").value);
                        setNodes((String)rMap.get("nodes").value);
                    }catch (Exception e) {
                        throw new BtException(className + " - FindNode decodingByte: 'r'对应值参数不匹配 "+e.getMessage());
                    }

                }else
                    throw new BtException(className+" - FindNode decodingByte: 'r'对应值非字典结构参数");
                continue;
            }
            throw new BtException(className+" - FindNode decodingByte: 字典结果异常");
        }
    }
    *
    * */
}
