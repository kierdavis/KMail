package com.kierdavis.kmail;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.InetSocketAddress;

public class WebServer {
    private KMail plugin;
    
    public WebServer(KMail plugin) {
        this.plugin = plugin;
    }
    
    public void start() {
        HttpServer server = HttpServer.create(new InetSocketAddress(4880));
        server.createContext("/", new WebHandler(plugin));
        server.setExecutor(null);
        server.start();
    }
}
