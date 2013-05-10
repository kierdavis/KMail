package com.kierdavis.kmail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WebHandler implements HttpHandler {
    private KMail plugin;
    private XMLMessageParser parser;
    
    public WebHandler(KMail plugin_) {
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
        }
        catch (XMLMessageParseException e) {
            statusCode = 400;
            response = "Could not parse request body: " + e.toString() + "\r\n";
        }
        
        if (msgs != null) {
            Iterator<Message> it = msgs.iterator();
            
            while (it.hasNext()) {
                plugin.getLogger().info("Received message via HTTP from " + t.getRemoteAddress().toString() + ": " + msg.getSrcAddress().toString() + " -> " + msg.getDestAddress().toString());
                plugin.receiveMessage(msg);
            }
        }
        
        t.sendResponseHeaders(statusCode, response.length());
        
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
