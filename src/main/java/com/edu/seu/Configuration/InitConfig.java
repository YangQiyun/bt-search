package com.edu.seu.Configuration;


import com.edu.seu.Util.BEncoding;
import org.springframework.stereotype.Component;

@Component
public class InitConfig {

    public int port=8000;

    public static BEncoding bEncoding=new BEncoding();
}
