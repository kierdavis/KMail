package com.kierdavis.kmail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

public class WebHandler implements HttpHandler {
    private KMail plugin;
    
    public WebHandler(KMail plugin) {
        this.plugin = plugin;
    }
    
    public void handle(HttpExchange t) throws IOException {
        String response;
        Message msg = null;
        
        try {
            InputStream is = t.getRequestBody();
            ObjectInputStream ois = new ObjectInputStream(is);
            msg = (Message) ois.readObject();
            ois.close();
            is.close();
        }
        
        catch (IOException e) {
            // Pass
        }
        
        catch (ClassNotFoundException e) {
            // Pass
        }
        
        if (msg == null) {
            response = "Could not parse request body.";
            t.sendResponseHeaders(400, response.length());
        }
        
        else {
            plugin.sendMessage(msg);
            
            response = "Mail received successfully.";
            t.sendResponseHeaders(200, response.length());
        }
        
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
