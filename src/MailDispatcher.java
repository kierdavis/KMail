package com.kierdavis.kmail;

import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.LinkedList;
import java.util.Queue;

public class MailDispatcher implements Runnable {
    private KMail plugin;
    private Queue<Message> queue;
    private boolean stopFlag;
    
    public MailDispatcher(KMail plugin) {
        this.plugin = plugin;
        this.queue = new LinkedList<Message>();
        this.stopFlag = false;
    }
    
    public synchronized boolean queueMessage(Message msg) {
        return queue.offer(msg);
    }
    
    public synchronized void stop() {
        stopFlag = true;
    }
    
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        while (true) {
            boolean stopFlag;
            Message msg;
            
            synchronized(this) {
                stopFlag = this.stopFlag;
                msg = queue.poll();
            }
            
            if (stopFlag) {
                break;
            }
            
            if (msg == null) {
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    // Ignore
                }
            }
            
            else {
                dispatch(msg);
            }
        }
    }
    
    public void dispatch(Message msg) {
        if (msg.getDestAddress().getHostname().equalsIgnoreCase(plugin.getLocalHostname())) {
            plugin.receiveMessage(msg);
        }
        else {
            dispatchRemote(msg);
        }
    }
    
    public void dispatchRemote(Message msg) {
        
    }
}
