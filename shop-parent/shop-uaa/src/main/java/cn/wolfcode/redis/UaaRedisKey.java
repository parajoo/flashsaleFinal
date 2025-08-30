package cn.wolfcode.redis;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * Created by wolfcode
 */
@Getter
public enum UaaRedisKey {
    USER_HASH("userHash"),USER_ZSET("userZset");
    UaaRedisKey(String prefix){
        this .prefix = prefix;
    }
    UaaRedisKey(String prefix, TimeUnit unit, int expireTime){
        this.prefix = prefix;
        this.unit = unit;
        this.expireTime = expireTime;
    }
    public String getRealKey(String key){
        return this.prefix+key;
    }
    private String prefix;
    private TimeUnit unit;
    private int expireTime;
}
