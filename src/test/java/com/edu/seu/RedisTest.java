package com.edu.seu;

import com.edu.seu.Protocol.TransactionInfo;
import com.edu.seu.enums.DHTMethodQvalue;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;

import static org.junit.Assert.*;


@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void setAndGet(){
        redisTemplate.opsForValue().set("test:set","testvalue");
        Assert.assertEquals("testvalue",redisTemplate.opsForValue().get("test:set"));
    }

    @Test
    public void serUser(){
        User user=new User();
        user.setUsername("yang");
        user.setUserpassword(110);
        redisTemplate.opsForValue().set("user",user);
        Assert.assertEquals("yang",((User)redisTemplate.opsForValue().get("user")).getUsername());
        Assert.assertSame(110,((User)redisTemplate.opsForValue().get("user")).getUserpassword());

    }

    //127.0.0.1:6379> config set stop-writes-on-bgsave-error no
    @Test
    public void TransactionInfo(){
        TransactionInfo transactionInfo=new TransactionInfo(DHTMethodQvalue.FINDNODE,"aa");
        redisTemplate.opsForValue().set(transactionInfo.getTid(),transactionInfo);
        Assert.assertEquals(DHTMethodQvalue.FINDNODE,((TransactionInfo)redisTemplate.opsForValue().get("aa")).getDhtMethodQvalue());
    }
}

@Data
class User implements Serializable{

    private String username;

    private Integer userpassword;
}