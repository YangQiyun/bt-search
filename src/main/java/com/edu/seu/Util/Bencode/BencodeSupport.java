package com.edu.seu.Util.Bencode;

import java.util.Map;

public interface BencodeSupport {
     //返回可编码格式
     Map<String,Object> toMap();

     //对字节序进行解码
    // void decodingByte(byte[] response,int start);
}
