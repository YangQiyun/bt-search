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
        String target="1619ecc9373c3639f4ee3e261638f29b33a6cbd6";
        String append="19ecc9373c3639f4ee3e261638f29b33a6cbd6";
        RoutingTable routingTable=new RoutingTable(HexString2Byte(target));
        //routingTable.put(HexString2Byte("4615ecc9373c3639f4ee3e261638f29b33a6cbd6"));
        List<byte[]> result=null;
        for(int i=10;i<100;i++){
            String c=String.valueOf(i)+append;
            new mThread(routingTable,HexString2Byte(c)).start();
            if(i==48)
                result=routingTable.get(HexString2Byte("1619ecc9373c3639f4ee3e261638f29b33a7cbd6"));
        }
        try {
            Thread.currentThread().sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<String> stringList=new ArrayList<>();
        for (byte[] argument:result){
            stringList.add(Byte2HexString(argument));
        }
        Assert.assertEquals(true,stringList.contains(target));
    }
}