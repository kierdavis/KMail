package com.kierdavis.kmail;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.HttpURLConnection;
import java.net.URL;
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
        String hostname = msg.getDestAddress().getHostname();
        
        if (hostname.indexOf(":") < 0) {
            hostname += ":4880";
        }
        
        plugin.getLogger().info("Sending message via HTTP to " + hostname + ": " + msg.getSrcAddress().toString() + " -> " + msg.getDestAddress().toString());
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();
        oos.close();
        
        byte[] requestBytes = bos.toByteArray();
        
        URL url = new URL("http://" + hostname + "/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-java-serialized-object");
        conn.setRequestProperty("Content-Length", Integer.toString(requestBytes.length));
        conn.setRequestProperty("Host", hostname);
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(false);
        
        OutputStream os = conn.getOutputStream();
        os.write(requestBytes);
        os.flush();
        os.close();
        
        conn.disconnect();
    }
}
