package com.kierdavis.kmail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebClient {
    private KMail plugin;
    
    public WebClient(KMail plugin) {
        this.plugin = plugin;
    }
    
    public void send(Message msg) {
        String hostname = msg.getDestAddress().getHostname();
        
        if (hostname.indexOf(":") < 0) {
            hostname += ":4880";
        }
        
        send(hostname, msg);
    }
    
    public void send(String hostname, Message msg) {
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
            conn.setReadTimeout(plugin.getClientTimeout() * 1000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-java-serialized-object");
            conn.setRequestProperty("Content-Length", Integer.toString(requestBytes.length));
            conn.setRequestProperty("Host", hostname);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            OutputStream os = conn.getOutputStream();
            os.write(requestBytes);
            os.flush();
            os.close();
            
            if (conn.getResponseCode() >= 300) {
                throw new IOException("Bad HTTP response from server: " + conn.getResponseMessage());
            }
            
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            String line = br.readLine();
            while (line != null) {
                line = br.readLine();
            }
            
            br.close();
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
            retry(msg);
        }
    }
    
    public void retry(Message msg) {
        msg.incRetries();
        
        if (msg.getRetries() >= plugin.getNumRetries()) {
            plugin.getLogger().severe("Sending failed permanently");
            
            Address srcAddr = new Address("CONSOLE", plugin.getLocalHostname());
            Message replyMsg = new Message(srcAddr, msg.getSrcAddress());
            
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Sending of the following message failed after ").append(plugin.getNumRetries()).append(" attempts:\n");
            bodyBuilder.append("  From: ").append(msg.getSrcAddress().toString()).append("\n");
            bodyBuilder.append("  To: ").append(msg.getDestAddress().toString()).append("\n");
            bodyBuilder.append("  Sent: ").append(msg.getSentDate().toString()).append("\n");
            bodyBuilder.append("  Body:\n    ").append(msg.getBody());
            replyMsg.setBody(bodyBuilder.toString());
            
            plugin.sendMessage(replyMsg);
        }
        
        else {
            plugin.sendMessage(msg);
        }
    }
}
