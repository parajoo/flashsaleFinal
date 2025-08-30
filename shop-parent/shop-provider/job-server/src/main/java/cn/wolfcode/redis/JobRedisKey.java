package cn.wolfcode.redis;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * Created by wolfcode
 */
@Getter
public enum JobRedisKey {
    USER_HASH("userHash"), USER_ZSET("userZset");

    JobRedisKey(String prefix, TimeUnit unit, int expireTime) {
        this.prefix = prefix;
        this.unit = unit;
        this.expireTime = expireTime;
    }

    JobRedisKey(String prefix) {
        this.prefix = prefix;
    }

    public String join(String key) {
        return this.prefix + key;
    }

    private String prefix;
    private TimeUnit unit;
    private int expireTime;
}
