package com.lock.order;

import com.lock.redis.RedisLock;
import com.lock.zookeeperlock.zookeeprLock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class User implements Runnable {

    private Lock zookeeperLock = new zookeeprLock();

    public void run() {
        zookeeperLock.lock();
        boolean salFlag = new Stock().sal();
        zookeeperLock.unlock();
        if (salFlag) {
            System.out.println(Thread.currentThread().getName() + "减库存成功--");
        } else {
            System.out.println(Thread.currentThread().getName() + "减库存失败—");
        }
    }

    public static void main(String[] args) {
        new Thread(new User(),"线程1-").start();
        new Thread(new User(),"线程2-").start();
    }
}
