package com.edu.seu.Util;

import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.RandomUtils;


import java.util.concurrent.atomic.AtomicInteger;

/*
* 获取transactionid
* 获取nodeid
*
* */
public class IdUtil {

    //用于递增消息ID
    private static AtomicInteger messageIDGenerator = new AtomicInteger(1);
    //递增刷新阈值
    private static int maxMessageID = 1<<15;

    /**
     * 生成一个递增的t,相当于消息id
     */
    public static String getTransactionID() {
        int result;
        //当大于阈值时,重置为0
        if ((result = messageIDGenerator.getAndIncrement()) > maxMessageID) {
            messageIDGenerator.lazySet(1);
        }
        return new String(ConvertUtil.int2TwoBytes(result), CharsetUtil.ISO_8859_1);
    }

    /**
     * 生成一个随机的nodeId
     */
    public static byte[] generateNodeId() {
        return RandomUtils.nextBytes(20);
    }
}
