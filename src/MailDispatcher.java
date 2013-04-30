package com.kierdavis.kmail;

import java.lang.Runnable;
import java.lang.Thread;
import java.util.LinkedList;
import java.util.Queue;

public class MailDispatcher implements Runnable {
    private KMail plugin;
    private Queue<Message> queue;
    
    public MailDispatcher(KMail plugin) {
        this.plugin = plugin;
        this.queue = new LinkedList<Message>();
    }
    
    public synchronized boolean queueMessage(Message msg) {
        return queue.offer(msg);
    }
    
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        while (true) {
            synchronized(this) {
                Message msg = queue.poll();
            }
            
            if (msg == null) {
                Thread.sleep(5000);
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
