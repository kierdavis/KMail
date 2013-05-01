package com.kierdavis.kmail;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {
    private KMail plugin;
    private HttpServer server;
    
    public WebServer(KMail plugin) {
        this.plugin = plugin;
    }
    
    public void start() throws IOException {
        if (server != null) {
            stop();
        }
        
        server = HttpServer.create(new InetSocketAddress(4880), 0);
        server.createContext("/", new WebHandler(plugin));
        server.setExecutor(null);
        server.start();
        
        plugin.getLogger().info("Listening on *:4880");
    }
    
    public void stop() {
        if (server == null) {
            return;
        }
        
        server.stop(5);
        server = null;
    }
}
