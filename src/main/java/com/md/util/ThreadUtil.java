package com.md.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//线程执行工具
public class ThreadUtil {
    private static final ExecutorService executor ;
    static {
        executor= Executors.newCachedThreadPool();
    }

    public static void startThread(Runnable work)
    {
        if(null!=work)
            executor.execute(work);
    }
}
