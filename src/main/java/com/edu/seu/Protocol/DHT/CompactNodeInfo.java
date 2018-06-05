package com.edu.seu.Protocol.DHT;

import com.edu.seu.Exception.BtException;
import com.edu.seu.Util.ConvertUtil;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.net.InetSocketAddress;

@Data
public class CompactNodeInfo {

    private byte[] nodeId;

    private String ip;

    private int port;


    private void storeInetSocketAddress(InetSocketAddress address){
        this.ip=address.getAddress().toString().substring(1);
        this.port=address.getPort();
    }

    /**
     * 获取节点的InetSocketAddress
     */
    public InetSocketAddress toAddress() {
        return new InetSocketAddress(this.ip, this.port);
    }

    /**
     * CompactNodeInfo 转 byte[]
     */
    public byte[] CompactNodeInfotoBytes() {
        return ConvertUtil.nodeAndaddress(nodeId,toAddress());
    }

    /**
     * byte[26] 转 CompactNodeInfo
     */
    public CompactNodeInfo(byte[] bytes) {
        if (bytes.length !=26)
            throw new BtException("转换为Node需要bytes长度为26,当前为:" + bytes.length);
        //nodeIds
        nodeId = ArrayUtils.subarray(bytes, 0, 20);

        //ip
        ip = ConvertUtil.bytes2Ip(ArrayUtils.subarray(bytes, 20, 24));

        //ports
        port = ConvertUtil.bytes2Port( ArrayUtils.subarray(bytes, 24, 26));

    }


    public CompactNodeInfo(byte[] nodeId,InetSocketAddress address){
        if(nodeId.length!=20)
            throw new BtException("node id不为20,当前为: "+nodeId.length);

        this.nodeId=nodeId;
        storeInetSocketAddress(address);
    }
}
