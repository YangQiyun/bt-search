package com.edu.seu.Protocol;

import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCYEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/*
* 通信的临时缓存内容
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HistoryInfo {

    //消息请求方式
    private DHTMethodQvalue dhtMethodQvalue;

    //消息编号的id
    private String tid;
}
