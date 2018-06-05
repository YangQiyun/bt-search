package com.edu.seu.Protocol;

import lombok.AllArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.edu.seu.Util.ConvertUtil.Byte2HexString;
import static com.edu.seu.Util.ConvertUtil.HexString2Byte;
import static org.junit.Assert.*;

public class RoutingTableTest {

    @AllArgsConstructor
    static class mThread extends Thread{

        private RoutingTable table;
        private byte[] id;

        @Override
        public void run() {
            table.put(id);
        }
    }

    @Test
    public void put() {
//        int a=8,b=128;
//        byte a1=(byte)a;
//        byte b1=(byte)b;
//        int d=0;
//        int e=(b1&0x0FF);
//        int c=a^b;
//        for(int i=0x080;i!=0;i>>=1){
//            if((c&i)==i){
//                System.out.println(i);
//            }
//        }

    }

    @Test
    public void get() {

    }
}