package com.lock.zookeeperlock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class zookeeprLock implements Lock {

    private static String LOCK_NAME = "/Lock";
    ThreadLocal<ZooKeeper> zooKeeperThreadLocal = new ThreadLocal<ZooKeeper>();
    ThreadLocal<String> CURRENT_LOCK = new ThreadLocal<String>();

    public void lock() {
        init();

        if (tryLock()) {
            System.out.println(Thread.currentThread().getName() + "获取到锁了");
        }
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        String nodeName = LOCK_NAME + "/zk_";
        try {
            CURRENT_LOCK.set(zooKeeperThreadLocal.get().create(nodeName, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
            List<String> nodeList = zooKeeperThreadLocal.get().getChildren(LOCK_NAME, false);
            Collections.sort(nodeList);
            String minNode = nodeList.get(0);
            System.out.println(Thread.currentThread().getName() + "当前节点锁是：" + CURRENT_LOCK.get());
            if (CURRENT_LOCK.get().equals(LOCK_NAME + "/" + minNode)) {
                return true;
            } else {
                System.out.println(Thread.currentThread().getName() + "等待锁---");
                String prevNode = nodeList.get(nodeList.indexOf(CURRENT_LOCK.get().substring(CURRENT_LOCK.get().lastIndexOf("/") + 1)));
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                final Stat prevExist = zooKeeperThreadLocal.get().exists(LOCK_NAME + "/" + prevNode, new Watcher() {
                    public void process(WatchedEvent watchedEvent) {
                        //代表你获取到锁
                        if (Event.EventType.NodeDeleted.equals(watchedEvent.getType())) {
                            countDownLatch.countDown();
                            System.out.println(Thread.currentThread().getName() + "唤醒锁");
                        }
                    }
                });
                if (prevExist != null) {
                    //阻塞
                    countDownLatch.await();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }


        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public void unlock() {

        try {
            System.out.println(Thread.currentThread().getName() + "删除节点" + CURRENT_LOCK);
            zooKeeperThreadLocal.get().delete(CURRENT_LOCK.get(), -1);
            CURRENT_LOCK.remove();
            zooKeeperThreadLocal.get().close();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Condition newCondition() {
        return null;
    }

    public void init() {
        if (zooKeeperThreadLocal.get() == null) {
            try {
                zooKeeperThreadLocal.set(new ZooKeeper("localhost:2181", 3000, new Watcher() {
                    public void process(WatchedEvent watchedEvent) {
                        System.out.println("已经触发了监听事件" + watchedEvent.getType() + "事件！");
                    }
                }));
                Stat stat = zooKeeperThreadLocal.get().exists(LOCK_NAME, false);
                if (stat == null) {
                    zooKeeperThreadLocal.get().create(LOCK_NAME, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }


        }
    }
}
