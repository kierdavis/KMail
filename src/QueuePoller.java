package com.kierdavis.kmail;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class QueuePoller extends BukkitRunnable {
    private KMail plugin;
    private BukkitTask task;
    
    public QueuePoller(KMail plugin_) {
        plugin = plugin_;
    }
    
    public void run() {
        plugin.pollQueues();
    }
    
    public void start() {
        task = runTaskTimer(plugin, 1 * 60 * 20, 15 * 60 * 20);
    }
    
    public void stop() {
        task.cancel();
    }
}
