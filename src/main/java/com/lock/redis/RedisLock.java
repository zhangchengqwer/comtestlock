package com.lock.redis;

import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class RedisLock implements Lock {

    private static String LOCK_NAME = "LOCK";
    private static String REQUEST_ID = "RedisLock";
    ThreadLocal<Jedis> jedisThreadLocal = new ThreadLocal<Jedis>();

    public void lock() {
        if (jedisThreadLocal.get() == null) {
            jedisThreadLocal.set(new Jedis("localhost"));
        }

        while (true) {
            if (tryLock()) {
                jedisThreadLocal.get().set(LOCK_NAME, REQUEST_ID);
                jedisThreadLocal.get().expire(LOCK_NAME, 3000);
                return;
            } else {
                System.out.println("等待锁...");
            }
        }
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        Long lockVal = jedisThreadLocal.get().setnx(LOCK_NAME, REQUEST_ID);
        if (lockVal > 0) {
            return true;
        }
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public void unlock() {
        final String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        jedisThreadLocal.get().eval(script, Collections.singletonList(LOCK_NAME),Collections.singletonList(REQUEST_ID));

    }

    public Condition newCondition() {
        return null;
    }
}
