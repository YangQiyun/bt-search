package com.edu.seu.Protocol.DHT;

import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class CompactNodeInfoTest {

    @Test
    public void getIp() {
        InetSocketAddress address=new InetSocketAddress("1.1.1.1",4451);
        String ip=address.getAddress().toString().substring(1);
        int a=1;
    }
}