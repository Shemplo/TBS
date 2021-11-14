package ru.shemplo.tbs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class TBSBackgroundExecutor implements AutoCloseable {
    
    private static volatile TBSBackgroundExecutor instance;
    
    public static TBSBackgroundExecutor getInstance () {
        if (instance == null) {
            synchronized (TBSClient.class) {
                if (instance == null) {
                    instance = new TBSBackgroundExecutor ();
                }
            }
        }
        
        return instance;
    }
    
    private final ExecutorService executors = Executors.newSingleThreadExecutor ();
    
    public void runInBackground (Runnable task) {
        executors.execute (task);
    }
    
    @Override
    public void close () throws Exception {
        log.info ("Stopping background executors...");
        executors.shutdown ();
    }
    
}
