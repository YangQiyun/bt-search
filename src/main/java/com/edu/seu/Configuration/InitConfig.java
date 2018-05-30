package com.edu.seu.Configuration;


import com.edu.seu.Protocol.Bencode.Bencoding;
import org.springframework.stereotype.Component;

@Component
public class InitConfig {

    public int port=8000;

    public static Bencoding bEncoding=new Bencoding();
}
