package com.hxl.plugin.scheduledinvokestarter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CoolRequestThreadPoolExecutor {
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static void submit(Runnable runnable) {
        threadPoolExecutor.submit(runnable);
    }
}
