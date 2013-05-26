package com.kierdavis.kmail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public class WebMailHandler implements HttpHandler {
    private KMail plugin;
    private XMLMessageParser parser;
    
    public WebMailHandler(KMail plugin_) {
        plugin = plugin_;
        parser = new XMLMessageParser();
    }
    
    public void handle(HttpExchange t) throws IOException {
        int statusCode = 200;
        String response = "OK";
        
        plugin.getLogger().info("Incoming connection via HTTP from " + t.getRemoteAddress().toString());
        
        InputStream is = t.getRequestBody();
        List<Message> msgs = null;
        
        try {
            msgs = parser.parse(is);
            is.close();
        }
        catch (XMLMessageParseException e) {
            statusCode = 400;
            response = "Could not parse request body: " + e.toString() + "\r\n";
        }
        
        if (msgs != null) {
            Iterator<Message> it = msgs.iterator();
            
            while (it.hasNext()) {
                Message msg = (Message) it.next();
                plugin.getLogger().info("Received message via HTTP from " + t.getRemoteAddress().toString() + ": " + msg.getSrcAddress().toString() + " -> " + msg.getDestAddress().toString());
                plugin.receiveMessage(msg);
            }
        }
        
        t.getResponseHeaders().add("Server", getServerHeader());
        t.sendResponseHeaders(statusCode, response.length());
        
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    public String getServerHeader() {
        StringBuilder b = new StringBuilder();
        b.append("KMail/");
        b.append(plugin.getDescription().getVersion());
        b.append(" Bukkit/");
        b.append(plugin.getServer().getVersion());
        return b.toString();
    }
}
