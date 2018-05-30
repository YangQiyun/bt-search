package com.edu.seu.Util;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 */
@Component
public class CacheUtil {

    /**
     * 发送记录缓存长度
     * 消息id(t)为2个字节,最大表示也就是2的16次方
     */
    private Integer sendCacheLen = 1<<16;
    /**
     * 发送记录缓存过期时间
     */
    private Integer sendCacheExpireMinute = 1;

    @Autowired
    public void init() {
        this.cache = Caffeine.newBuilder()
                .initialCapacity(sendCacheLen)
                .maximumSize(sendCacheLen)
                .expireAfterAccess(sendCacheExpireMinute, TimeUnit.MINUTES)
                //传入缓存加载策略,key不存在时调用该方法返回一个value回去
                //此处直接返回空
                .build(k1 -> null);
    }

    //创建缓存
    private static LoadingCache<String, HistoryInfo> cache ;


    /**
     * 获取数据
     */
    public static HistoryInfo get(String key) {
        return cache.getIfPresent(key);
    }

    /**
     * 获取并删除
     */
    public static HistoryInfo getAndRemove(String key) {
        HistoryInfo result = cache.getIfPresent(key);
        if(result != null){
            cache.invalidate(key);
        }
        return result;
    }

    /**
     * 存入数据
     */
    public static void put(String key, HistoryInfo channelCache) {
        cache.put(key, channelCache);
    }

    /**
     * 删除数据
     */
    public static void remove(String key) {
        cache.invalidate(key);
    }
}
