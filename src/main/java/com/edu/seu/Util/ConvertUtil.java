package com.edu.seu.Util;

import com.edu.seu.Exception.BtException;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class ConvertUtil {

    private static int[] init2ByteArray;

    static {
        init2ByteArray=new int['f'+1];
        for(int i='0';i<='9';i++)
            init2ByteArray[i]=i-'0';
        for (int i='a';i<='f';i++)
            init2ByteArray[i]=i-'a'+10;
        for(int i=0;i<=9;i++)
            init2ByteArray[i]=i+'0';
        for (int i=10;i<=15;i++)
            init2ByteArray[i]=i+'a'-10;
    }

    /*
    *  HexString表示的16位bit字节串转换为实际的以byte存储的对应的二进制串
    * eg: string:909f9cbdedf4f7e29e820e3fd5e00a2965450b8a
    *     to
    *     byte: 90 9f 9c bd ed f4 f7 e2 9e 82 0e 3f d5 e0 0a 29 65 45 0b 8a
    *     转化为对应的20个字节大小的二进制串
    * */
    public  static byte[] HexString2Byte(String target){

        //将转化的字符串全置为小写
        target=target.toLowerCase();

        //检查待转化的字符串格式是否正确
        if(target.length()%2!=0)
            throw new RuntimeException("二进制串长度出错");
        for(int i=0;i<target.length();i++){
            if(!((target.charAt(i)>='0'&&target.charAt(i)<='9')||(target.charAt(i)>='a'&&target.charAt(i)<='f'))){
                throw new RuntimeException("二进制串格式出错");
            }
        }

        //每次提取两个char进行高低位偏移赋值到一个byte中
        byte[] value=new byte[target.length()/2];
        int LOW_MASK=0xFF;
        for(int i=0;i<target.length()/2;i++){
            byte high=(byte)(init2ByteArray[target.charAt(i*2)]&LOW_MASK);
            byte low=(byte)(init2ByteArray[target.charAt(i*2+1)]&LOW_MASK);
            value[i]=(byte)(high<<4|low);
        }

        return value;
    }


    /*
    * 将byte二进制串转化为HexString格式
    * */
    public static String Byte2HexString(byte[] target){
        StringBuilder stringBuilder=new StringBuilder(target.length*2);
        //进行有效性判断
        if(target==null||target.length<=0)
            throw new RuntimeException("byteArray error");

        //对高低位分别掩码取值通过数组直接快速定位具体的表示值
        for(int i=0;i<target.length;i++){
            int high=(target[i]&0xFF)>>4;
            int low=target[i]&0x0F;
            stringBuilder.append((char) init2ByteArray[high]);
            stringBuilder.append((char) init2ByteArray[low]);
        }

        return stringBuilder.toString().toLowerCase();
    }

    /**
     * unsigned byte 转 int
     */
    public static int Byte2Int(byte data){
        return data&0x0FF;
    }


    /**
     * int 转 2个字节的byte[]
     * 舍弃16位最高位,只保留16位,两个字节的低位.
     * 这个字节数组的顺序需要是这样的.. 目前我收到其他节点的信息,他们的字节数组大多是这样的/
     * 并且按照惯性思维,左边的(也就是byte[0]),的确应该是高位的.
     */
    public static byte[] int2TwoBytes(int value) {
        byte[] des = new byte[2];
        des[1] = (byte) (value & 0xff);
        des[0] = (byte) ((value >> 8) & 0xff);
        return des;
    }

    /**
     * byte[4] 转 string with "." split ip
     */
    public static String bytes2Ip(byte[] ipBytes) {
        if (ipBytes.length != 4) {
            throw new BtException("bytes2Ip失败,bytes长度不为4.当前长度:" + ipBytes.length);
        }
        return String.join(".", Integer.toString(ipBytes[0] & 0xFF), Integer.toString(ipBytes[1] & 0xFF)
                , Integer.toString(ipBytes[2] & 0xFF), Integer.toString(ipBytes[3] & 0xFF));
    }


    /**
     *   string with "." split ip 转 byte[4]
     */
    public static byte[] ip2Bytes(String ip) {
        if (StringUtils.isBlank(ip)) {
            throw new BtException("ip2Bytes失败,ip为空");
        }
        String[] ips = ip.split("\\.");
        if (ips.length != 4) {
            throw new BtException("ip2Bytes失败,ip的段数长度不为4.ip:" + ip);
        }
        return new byte[]{
                Integer.valueOf( ips[0]).byteValue(),
                Integer.valueOf( ips[1]).byteValue(),
                Integer.valueOf( ips[2]).byteValue(),
                Integer.valueOf( ips[3]).byteValue(),
        };
    }

    /**
     * byte[2] 转 int port
     * 大端序
     */
    public static int bytes2Port(byte[] portBytes) {
        if (portBytes.length != 2) {
            throw new BtException("bytes2Port失败,bytes长度不为2.当前长度:" + portBytes.length);
        }
        return portBytes[1] & 0xFF | (portBytes[0] & 0xFF) << 8;
    }

    public static byte[] getNode(){
        return HexString2Byte("909f9cbdedf4f7e29e820e3fd5e00a2965450b8a");
    }

    /**
     * nodeId 和 address合并成一个compactInfo的字节数组
     */
    public static byte[] nodeAndaddress(byte[] nodeId, InetSocketAddress address){

        //nodeIds
        byte[] nodeBytes = new byte[26];
        System.arraycopy(nodeId, 0, nodeBytes, 0, 20);

        //ip
        String ip=address.getAddress().toString().substring(1);
        String[] ips = StringUtils.split(ip, ".");
        if(ips.length != 4)
            throw new BtException("该节点IP有误,节点信息:");
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(ips[i]);
        }
        System.arraycopy(ipBytes, 0, nodeBytes, 20, 4);

        //ports
        byte[] portBytes = ConvertUtil.int2TwoBytes(address.getPort());
        System.arraycopy(portBytes, 0, nodeBytes, 24, 2);

        return nodeBytes;
    }

    public static void main(String []args){
        String target="1619ecc9373c3639f4ee3e261638f29b33a6cbd6";
        byte[] result=HexString2Byte(target);

        String temp=new String(result, CharsetUtil.ISO_8859_1);
        String b=new String(result,0,1, CharsetUtil.ISO_8859_1);
        byte[] a=temp.getBytes(CharsetUtil.ISO_8859_1);
        System.out.println(temp+"  "+temp.length());

        String first="中文";
        byte[] t=first.getBytes();
        byte[] q=new String(first.getBytes(),CharsetUtil.US_ASCII).getBytes(CharsetUtil.US_ASCII);
        System.out.println(t+" "+q);


       // System.out.println(Byte2HexString(result).equals(target));

        //System.out.println(Byte2HexString(":".getBytes()));
    }
}
