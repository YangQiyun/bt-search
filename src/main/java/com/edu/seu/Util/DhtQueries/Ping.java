package com.edu.seu.Util.DhtQueries;

/*
* DHT协议中的ping请求
*
* 最基础的请求就是ping。"q"="ping"一个ping请求有一个单一的参数，
* "id"的值是一个表示发送请求的node的ID的20字节长的二进制字符串网络字节序。
* 完整合适的应答ping也需要一个单一的键"id"包含了应答node的node ID序列号。
*
* ping Query = {"t":"aa", "y":"q", "q":"ping", "a":{"id":"abcdefghij0123456789"}}
* Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
* */

import com.edu.seu.Configuration.InitConfig;
import com.edu.seu.Exception.BtException;
import com.edu.seu.Util.BEncoding;

import java.util.Map;
import java.util.TreeMap;

public class Ping extends KRPC{

    private String className=this.getClass().getName();

    public Ping(String t,String id){
        setY(yType.QUERY);
        setQ("ping");
        setT(t);
        setA(new Entry("id",id));
    }

    public Ping(){
        setY(yType.QUERY);
        setQ("ping");
        setT("aa");
        setA(null);
    }

    //只针对返回ping的byte字节序进行解析
    public Ping(byte[] response,int start){
        //进行解码获取内容
        BEncoding.btDecodeResult result= InitConfig.bEncoding.decodingObject(response,start);
        //解码结果类型判断
        if(result.type!=BEncoding.beType.Dictionary)
            throw new BtException(className+" - Ping constructor: 解析字节序出错非字典结构");
        //对解码结果进行分派信息录入
        Map<String,BEncoding.btDecodeResult> resultMap= (Map<String, BEncoding.btDecodeResult>) result;
        for(Map.Entry<String,BEncoding.btDecodeResult> entry:resultMap.entrySet()){
            if(entry.getKey().equals("t")){
                setT((String) entry.getValue().value);
                // TODO: 18-5-28 对应的消息编码ID的消除
                continue;
            }
            if(entry.getKey().equals("y")){
                if(entry.getValue().type==BEncoding.beType.ByteString){
                   if(((String)entry.getValue().value).equals("r"))
                       setY(yType.RESPONSE);
                   else
                       throw new BtException(className+" - Ping constructor: 'y'对应值非'r'");
                }else
                    throw new BtException(className+" - Ping constructor: 'y'对应值非bytestring");
                continue;
            }
            if(entry.getKey().equals("r")){
                if(entry.getValue().type==BEncoding.beType.Dictionary){
                    Map<String,BEncoding.btDecodeResult> rMap= (Map<String, BEncoding.btDecodeResult>) entry.getValue().value;
                    if(rMap.size()!=1)
                        throw new BtException(className+" - Ping constructor: 'r'对应值参数个数大于一个");
                    try {
                        setId((String) rMap.get("id").value);
                    }catch (Exception e) {
                        throw new BtException(className + " - Ping constructor: 'y'对应值参数不匹配 "+e.getMessage());
                    }

                }else
                    throw new BtException(className+" - Ping constructor: 'r'对应值非字典结构参数");
                continue;
            }
            throw new BtException(className+" - Ping constructor: 字典结果异常");
        }
    }

    public void setId(String id){
        setA(new Entry("id",id));
    }

    public void setTid(String tid){
        setT(tid);
    }

    public Map<String,BEncoding.btDecodeResult> toMap(){
        Map<String,BEncoding.btDecodeResult> resultMap=new TreeMap<>();
        resultMap.put("t",BEncoding.newStringResult(getT()));
        resultMap.put("y",BEncoding.newStringResult("q"));
        resultMap.put("q",BEncoding.newStringResult(getQ()));
        resultMap.put("a",BEncoding.newStringResult(getA()));
        return resultMap;
    }

}
