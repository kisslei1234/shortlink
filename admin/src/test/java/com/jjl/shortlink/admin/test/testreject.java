package com.jjl.shortlink.admin.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class testreject implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if(executor.isShutdown()){
            return;
        }
        BlockingQueue<Runnable> queue = executor.getQueue();
        if (queue.poll() != null) {
            Runnable task = queue.poll();
            if (task != null) {
                task.run();
            }
            queue.offer(r);
        }else {
            System.out.println("任务被拒绝");
        }
    }
}
