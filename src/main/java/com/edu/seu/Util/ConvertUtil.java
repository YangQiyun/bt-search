package com.edu.seu.Util;

import io.netty.util.CharsetUtil;
import org.slf4j.LoggerFactory;

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

    public static byte[] getNode(){
        return HexString2Byte("909f9cbdedf4f7e29e820e3fd5e00a2965450b8a");
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
