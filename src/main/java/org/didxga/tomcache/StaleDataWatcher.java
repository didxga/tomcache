package org.didxga.tomcache;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StaleDataWatcher {

    private ScheduledExecutorService executorService;
    private MemoryCacheRepository repository;

    public StaleDataWatcher(MemoryCacheRepository repository) {
        this.repository = repository;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Iterator<Key> keys = repository.cache.keySet().iterator();
                Date now = new Date();
                while (keys.hasNext()) {
                  Key key = keys.next();
                  if(key.dueDate.before(now)) {
                      keys.remove();
                  }
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.executorService.shutdownNow();
    }
}
