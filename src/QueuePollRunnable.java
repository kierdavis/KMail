package com.kierdavis.kmail;

import java.lang.Runnable;

public class QueuePollRunnable implements Runnable {
    private KMail plugin;
    
    public QueuePollRunnable(KMail plugin_) {
        plugin = plugin_;
    }
    
    public void run() {
        plugin.pollQueuesAsync();
    }
}
