package com.edu.seu.Util.KRPC;


import com.edu.seu.Util.Bencode.BencodeSupport;
import com.edu.seu.Util.Bencode.Bencoding;
import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCYEnum;

import java.util.Map;
import java.util.TreeMap;


//类似门面模式，进行对KRPC相关接口的保护
/*
* 在KRPC基本的 t y v基础上
* query请求包括两个额外的键对"q"和"a"，"q"的值表示的是请求方式的名字，"a"的值表示请求方式所附带的参数字典集。
* */
public class Queries implements BencodeSupport{

    private KRPC mQuery=new KRPC();

    //构造函数对KRPC协议进行query初始化
    public Queries(){
        // TODO: 18-5-28 使用动态的tid
        mQuery.setT("aa");
        mQuery.setY(KRPCYEnum.QUERY);
        mQuery.setV("");
    }

    //默认是不需要tid编号的
    public Queries(DHTMethodQvalue qValue){
        this();
        setMethod(qValue);
    }

    public void setMethod(DHTMethodQvalue qValue){
        mQuery.setQ(qValue.getCode());
    }

    public void setTid(String tid){
        mQuery.setT(tid);
    }

    public String getTid(){
        return mQuery.getT();
    }

    public KRPCYEnum getYtype(){
        return mQuery.getY();
    }


    public void setArgs(Map map){
        mQuery.setA(map);
    }

    public Map getArgs(){
        return mQuery.getA();
    }

    public DHTMethodQvalue getMethod(){
        DHTMethodQvalue[] methodQvalues=DHTMethodQvalue.values();
        for (DHTMethodQvalue value:methodQvalues){
            if(value.getCode().equals(mQuery.getQ()))
                return value;
        }
       return DHTMethodQvalue.UNKNOWN;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> resultMap=new TreeMap<>();
        resultMap.put("t",getTid());
        resultMap.put("y",getYtype().getCode());
        resultMap.put("q",getMethod().getCode());
        resultMap.put("a",getArgs());
        return resultMap;
    }

}
