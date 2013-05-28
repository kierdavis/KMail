package com.kierdavis.kmail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WebClient {
    private KMail plugin;
    private XMLMessageSerializer serializer;
    
    public WebClient(KMail plugin_) {
        plugin = plugin_;
        serializer = new XMLMessageSerializer();
    }
    
    public void send(Message msg) {
        String addr;
        
        if (msg.hasSendVia()) {
            addr = msg.getSendVia();
        }
        else {
            addr = msg.getDestAddress().getHostname();
        }
        
        if (addr.indexOf(":") < 0) {
            addr += ":4880";
        }
        
        send(addr, msg);
    }
    
    public void send(String addr, Message msg) {
        plugin.getLogger().info("Sending message via HTTP to " + addr + ": " + msg.getSrcAddress().toString() + " -> " + msg.getDestAddress().toString());
        
        boolean success = false;
        HttpURLConnection conn = null;
        
        try {
            Collection<Message> msgs = new ArrayList<Message>();
            msgs.add(msg);
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.serialize(bos, msgs);
            byte[] requestBytes = bos.toByteArray();
            
            URL url = new URL("http://" + addr + "/");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(plugin.getClientTimeout() * 1000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setRequestProperty("Content-Length", Integer.toString(requestBytes.length));
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
            
            //InputStream is = conn.getInputStream();
            //InputStreamReader isr = new InputStreamReader(is);
            //BufferedReader br = new BufferedReader(isr);
            //
            //String line = br.readLine();
            //while (line != null) {
            //    line = br.readLine();
            //}
            //
            //br.close();
            success = true;
        }
        
        catch (XMLMessageSerializationException e) {
            plugin.getLogger().severe("Could not serialize messages: " + e.toString());
        }
        
        catch (IOException e) {
            plugin.getLogger().severe("Could not send messages: " + e.toString());
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
            
            msg = plugin.errorResponse(msg, "Sending failed after " + Integer.toString(plugin.getNumRetries()) + " attempts");
        }
        
        plugin.sendMessage(msg);
    }
    
    public List<Message> pollQueue(String addr) {
        plugin.getLogger().info("Polling queue at " + addr + " for new messages");
        
        HttpURLConnection conn = null;
        List<Message> messages = null;
        
        try {
            String hostname = URLEncoder.encode(plugin.getLocalHostname(), "UTF-8");
            URL url = new URL("http://" + addr + "/fetch?hostname=" + hostname);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(plugin.getClientTimeout() * 1000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoOutput(false);
            conn.setDoInput(true);
            
            if (conn.getResponseCode() >= 300) {
                throw new IOException("Bad HTTP response from server: " + conn.getResponseMessage());
            }
            
            XMLMessageParser parser = new XMLMessageParser();
            InputStream is = conn.getInputStream();
            messages = parser.parse(is);
            is.close();
        }
        
        catch (XMLMessageParseException e) {
            plugin.getLogger().severe("Could not parse response: " + e.toString());
        }
        
        catch (IOException e) {
            plugin.getLogger().severe("Could not fetch messages: " + e.toString());
        }
        
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        
        return messages;
    }
}
