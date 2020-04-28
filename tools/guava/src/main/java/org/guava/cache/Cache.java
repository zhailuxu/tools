package org.guava.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

/**
 * https://ifeve.com/google-guava-cachesexplained/
 *
 * @author luxu.zlx
 */
public class Cache {

    static ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    static LoadingCache<String, String> cacheAutoRefresh = CacheBuilder.newBuilder().maximumSize(1000)
            .concurrencyLevel(16)
            .refreshAfterWrite(5, TimeUnit.SECONDS).build(new CacheLoader<String, String>() {

                @Override
                public String load(String key) throws Exception {
                    // TODO Auto-generated method stub
                    return "value";
                }

                //test1
                @Override
                public ListenableFuture<String> reload(String key, String value) throws Exception {
                    // TODO Auto-generated method stub
                    // 同步
                    // return Futures.immediateFuture(value);
 
                    // 异步计算新值
                    ListenableFutureTask<String> task = ListenableFutureTask.create(new Callable<String>() {
 
                        @Override
                        public String call() throws Exception {
                            // TODO Auto-generated method stub
                            return "new value";
                        }

                    });

                    pool.execute(task);

                    return task;

                }

            });

    static LoadingCache<String, String> cacheAutoExpire = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.SECONDS).removalListener(new RemovalListener<String, String>() {

                public void onRemoval(RemovalNotification<String, String> notification) {

                    System.out.println(Joiner.on("_").join("onRemoval", notification.getCause(), notification.getKey(),
                            notification.getValue()));
                }
            }).build(new CacheLoader<String, String>() {
                public String load(String key) {

                    return "value";
                }
            });

    static void testAutoExpire() {
        System.out.println(cacheAutoExpire.getUnchecked("a"));
        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println(cacheAutoExpire.getIfPresent("a"));
                if (null == cacheAutoExpire.getIfPresent("a")) {

                    // 主动回收过期的
                    // cacheAutoExpire.cleanUp();
                    // 任何时候，你都可以显式地清除缓存项，而不是等到它被回收：
                    cacheAutoExpire.invalidate("a");
                }

            }
        }).start();
        ;
    }

    static void testAutoRefresh() throws InterruptedException {
        System.out.println(cacheAutoRefresh.getUnchecked("a"));
        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println(cacheAutoRefresh.getIfPresent("a"));

            }
        }).start();
        ;
    }

    public static void main(String[] args) throws InterruptedException {
        // testAutoRefresh();
        testAutoExpire();

    }

}
