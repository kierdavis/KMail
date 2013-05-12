package com.kierdavis.kmail;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {
    private KMail plugin;
    private HttpServer server;
    
    public WebServer(KMail plugin_) {
        plugin = plugin_;
    }
    
    public void start() throws IOException {
        if (server != null) {
            stop();
        }
        
        String ip = plugin.getServerIP();
        int port = plugin.getServerPort();
        
        server = HttpServer.create(new InetSocketAddress(ip, port), 0);
        server.createContext("/", new WebMailHandler(plugin));
        server.setExecutor(null);
        server.start();
        
        plugin.getLogger().info("Listening on " + ip + ":" + Integer.toString(port));
    }
    
    public void stop() {
        if (server == null) {
            return;
        }
        
        server.stop(2);
        server = null;
    }
}
