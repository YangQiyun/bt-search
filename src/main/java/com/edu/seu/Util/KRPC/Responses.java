package com.edu.seu.Util.KRPC;

import com.edu.seu.Util.Bencode.BencodeSupport;
import com.edu.seu.Util.Bencode.Bencoding;
import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCYEnum;
import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;

import java.util.Map;
import java.util.TreeMap;


//类似门面模式，进行对KRPC相关接口的保护
/*
 * 在KRPC基本的 t y v基础上
 * response应答包括一个额外的键对“r","r"的值表示返回的参数字典集。
 * */
public class Responses implements BencodeSupport{

    private KRPC mResponse=new KRPC();

    //构造函数对KRPC协议进行response初始化
    public Responses(){
        // TODO: 18-5-28 动态的tid
        mResponse.setT("aa");
        mResponse.setY(KRPCYEnum.RESPONSE);
        mResponse.setV("");
    }


    public void setTid(String tid){
        mResponse.setT(tid);
    }

    public String getTid(){
        return mResponse.getT();
    }

    public KRPCYEnum getYtype(){
        return mResponse.getY();
    }


    protected void setArgs(Map map){
        mResponse.setR(map);
    }

    protected Map getArgs(){
        return mResponse.getR();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> resultMap=new TreeMap<>();
        resultMap.put("t",getTid());
        resultMap.put("y",getYtype().getCode());
        resultMap.put("r",getArgs());
        return resultMap;
    }


/*
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
    }*/
}
