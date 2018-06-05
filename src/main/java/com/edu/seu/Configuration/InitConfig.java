package com.edu.seu.Configuration;


import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Util.ConvertUtil;
import com.edu.seu.Util.IdUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "onion")
public class InitConfig {


    public int port=8000;

    public static Bencoding bEncoding=new Bencoding();

    /**
     * UDP服务器端端口号
     */
    private List<Integer> ports = new ArrayList<>();

    /**初始地址*/
    private List<String> initAddresses = new LinkedList<>();

    /**
     * nodeIds
     */
    private List<String> nodeIds = new ArrayList<>();

    /**
     * 我的token内容
     */
    public static String token="yang";

    /**
     * 获取初始化地址
     */
    public InetSocketAddress[] getInitAddressArray() {
        return this.initAddresses.stream().map(item -> {
            String[] split = item.split(":");
            return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
        }).toArray(InetSocketAddress[]::new);
    }

    /**
     * 初始化nodeId
     */
    public void initNodeIds() {
        for (int i = 0; i < this.ports.size(); i++) {
            nodeIds.add(ConvertUtil.Byte2HexString(IdUtil.generateNodeId()));
        }
    }

}
