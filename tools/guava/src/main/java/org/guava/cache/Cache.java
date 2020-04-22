package org.guava.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class Cache {

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

	public static void main(String[] args) {

		System.out.println(cacheAutoExpire.getUnchecked("a"));
		new Thread(() -> {
			for (;;) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(cacheAutoExpire.getIfPresent("a"));
//				if (null == cacheAutoExpire.getIfPresent("a")) {
//					cacheAutoExpire.cleanUp();
//				}

			}
		}).start();
		;

	}

}
