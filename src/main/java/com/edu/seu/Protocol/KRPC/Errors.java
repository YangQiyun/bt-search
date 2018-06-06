package com.edu.seu.Protocol.KRPC;


import com.edu.seu.Util.IdUtil;
import com.edu.seu.enums.KRPCErrorEnum;
import com.edu.seu.enums.KRPCYEnum;

import java.util.ArrayList;
import java.util.List;


//类似门面模式，进行对KRPC相关接口的保护
/*
 * 在KRPC基本的 t y v基础上
 * error应答包含一个额外的键对"e"，"e"的值是list类型。第一个参数是一个代表错误代码的整型数。第二个参数是一个包含错误信息的string。
 * */
public class Errors {

    private KRPC mError=new KRPC();



    public String receivedContent;

    //构造函数对KRPC协议进行response初始化
    public Errors(){
        mError.setT(IdUtil.getTransactionID());
        mError.setY(KRPCYEnum.ERROR);
        mError.setV("");
    }

    public void setTid(String tid){
        mError.setT(tid);
    }



    public void setError(KRPCErrorEnum errorType){
        List<Object> args=mError.getE();
        if(args==null)
           args=new ArrayList<>();
        args.add(errorType.getCode());
        args.add(errorType.getMessage());
        mError.setE(args);
    }

    public KRPCErrorEnum getError(){
        List<Object> args=mError.getE();
        KRPCErrorEnum[] values=KRPCErrorEnum.values();
        for (KRPCErrorEnum errorEnum:values){
            if (errorEnum.getCode()==args.get(0))
                return errorEnum;
        }
        return KRPCErrorEnum.UNKNOWN;
    }

    public String getReceivedContent() {
        return receivedContent;
    }

    public void setReceivedContent(String receivedContent) {
        this.receivedContent = receivedContent;
    }
}
