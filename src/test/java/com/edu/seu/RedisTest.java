package com.edu.seu;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

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
}