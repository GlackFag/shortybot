package com.glackfag.shortybot.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class AutoDeletingConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private final Timer timer;
    @Setter
    @Getter
    private Long timeout;

    public AutoDeletingConcurrentHashMap(@NonNull Long timeout) {
        timer = new Timer(true);
        this.timeout = timeout;
    }

    @Override
    public V put(K key, V value){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                remove(key);
            }
        }, timeout, timeout);

        return super.put(key, value);
    }
}
