package com.kierdavis.kmail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public class MailDispatcher implements Runnable {
    public static final int MAX_RETRIES = 3;
    
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
        
        boolean success = false;
        HttpURLConnection conn = null;
        
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(msg);
            oos.flush();
            oos.close();
            
            byte[] requestBytes = bos.toByteArray();
            
            URL url = new URL("http://" + hostname + "/");
            conn = (HttpURLConnection) url.openConnection();
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
            success = true;
        }
        
        catch (IOException e) {
            plugin.getLogger().severe("Error sending message: " + e.toString());
        }
        
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        
        if (!success) {
            msg.incRetries();
            
            if (msg.getRetries() == MAX_RETRIES) {
                plugin.getLogger().severe("Sending failed permanently");
                
                Address srcAddr = new Address("CONSOLE", plugin.getLocalHostname());
                Message replyMsg = new Message(srcAddr, msg.getSrcAddress());
                
                StringBuilder bodyBuilder = new StringBuilder();
                bodyBuilder.append("Sending of the following message failed after ").append(MAX_RETRIES).append(" attempts:\n");
                bodyBuilder.append("  From: ").append(msg.getSrcAddress().toString()).append("\n");
                bodyBuilder.append("  To: ").append(msg.getDestAddress().toString()).append("\n");
                bodyBuilder.append("  Sent: ").append(msg.getSentDate().toString()).append("\n");
                bodyBuilder.append("  Body:\n    ").append(msg.getBody());
                replyMsg.setBody(bodyBuilder.toString());
                
                queueMessage(replyMsg);
            }
            
            else {
                queueMessage(msg);
            }
        }
    }
}
