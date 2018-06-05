package com.edu.seu.Protocol;

import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCYEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/*
* 通信的临时缓存内容
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
//chain 一个布尔值。如果为真，产生的setter返回的this而不是void
@Accessors(chain = true)
public class TransactionInfo implements Serializable{

    //消息请求方式
    private DHTMethodQvalue dhtMethodQvalue;

    //消息编号的id
    private String tid;

}
