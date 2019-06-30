package com.lock.zookeeperlock;

import org.apache.zookeeper.*;

import java.io.IOException;

public class zookeeperLockClient {
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 3000, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                System.out.println("已经触发了监听事件" + watchedEvent.getType() + "事件！");
            }
        });
        zooKeeper.create("/zookeeperLock", "zookeeperLockData".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        //zooKeeper.create()

    }
}