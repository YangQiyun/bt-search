package com.edu.seu.Util;

import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCYEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
