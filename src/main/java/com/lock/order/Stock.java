package com.lock.order;

import com.lock.redis.RedisLock;

public class Stock {

    private static int num = 1;

    public boolean sal() {
        if (num > 0) {
            num--;
            System.out.println(Thread.currentThread().getName() + "sal---");
            return true;
        }
            return false;
    }

}
