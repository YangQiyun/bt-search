package com.edu.seu.Util.DHT;


import com.edu.seu.Util.Bencode.Bencoding;
import com.edu.seu.Util.KRPC.Queries;

import java.util.Map;

/*
*所有的请求都有一个"id"键，它的值表示请求node的node ID。所有的应答也有一个"id"键,它的值表示回应的node的node ID。
* */
public interface DHT {

    String getID();

    void setID(String id);



}
