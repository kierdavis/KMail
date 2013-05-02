package com.kierdavis.kmail;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KMail extends JavaPlugin {
    private Map<Player, PartialMessage> partialMessages;
    private Map<String, Mailbox> mailboxes;
    private MailDispatcher dispatcher;
    private WebServer server;
    
    public void onEnable() {
        // Ensure config.yml exists
        saveDefaultConfig();
        
        // Initialise variables
        partialMessages = new HashMap<Player, PartialMessage>();
        mailboxes = new HashMap<String, Mailbox>();
        dispatcher = new MailDispatcher(this);
        server = new WebServer(this);
        
        // Register event listeners
        new PartialMessageListener(this);
        
        // Register commands
        getCommand("kmail").setExecutor(new KMailCommandExecutor(this));
        
        // Start out threads
        getLogger().info("Starting mail dispatcher");
        dispatcher.start();
        
        getLogger().info("Starting web server");
        try {
            server.start();
        }
        catch (IOException e) {
            getLogger().severe("Could not start web server: " + e.toString());
        }
    }
    
    public void onDisable() {
        // Stop our threads
        getLogger().info("Stopping mail dispatcher");
        dispatcher.stop();
        
        getLogger().info("Stopping web server");
        server.stop();
    }
    
    public String getDefaultLocalHostname() {
        return getServer().getServerName().toLowerCase();
    }
    
    public String getDefaultServerIP() {
        return getServer().getIp();
    }
    
    public String getLocalHostname() {
        return getConfig().getString("local.hostname", getDefaultLocalHostname());
    }
    
    public int getClientTimeout() {
        return getConfig().getInt("remote.client.timeout", 15);
    }
    
    public int getNumRetries() {
        return getConfig().getInt("remote.client.retries", 3);
    }
    
    public String getServerIP() {
        return getConfig().getString("remote.server.ip", getDefaultServerIP());
    }
    
    public int getServerPort() {
        return getConfig().getInt("remote.server.port", 4880);
    }
    
    public Mailbox getMailbox(String username) {
        Mailbox mb = mailboxes.get(username.toLowerCase());
        
        if (mb == null) {
            mb = Mailbox.load(this, username);
            if (mb == null) {
                mb = new Mailbox();
            }
            
            mailboxes.put(username.toLowerCase(), mb);
        }
        
        return mb;
    }
    
    public void receiveMessage(Message msg) {
        String username = msg.getDestAddress().getUsername();
        getMailbox(username).add(msg);
        
        if (username.equalsIgnoreCase("CONSOLE")) {
            getLogger().info("\247eIncoming mail from \247a" + msg.getSrcAddress().toString() + "\247e.");
            getLogger().info("\247eType \2474kmail read\247e to view it.");
        }
        
        else {
            Player player = getServer().getPlayer(username);
            if (player != null) {
                player.sendMessage("\247eIncoming mail from \247a" + msg.getSrcAddress().toString() + "\247e.");
                player.sendMessage("\247eType \2474/kmail read\247e to view it.");
            }
        }
    }
    
    public synchronized void sendMessage(Message msg) {
        if (msg.getSrcAddress().getHostname().equals("local")) {
            msg.getSrcAddress().setHostname(getLocalHostname());
        }
        if (msg.getDestAddress().getHostname().equals("local")) {
            msg.getDestAddress().setHostname(getLocalHostname());
        }
        
        msg.setSentDate(new Date());
        
        if (!dispatcher.queueMessage(msg)) {
            getLogger().severe("Failed to queue message from " + msg.getSrcAddress().toString());
        }
    }
    
    public synchronized PartialMessage getPartialMessage(Player player) {
        return partialMessages.get(player);
    }
    
    public synchronized void putPartialMessage(Player player, PartialMessage pm) {
        partialMessages.put(player, pm);
    }
    
    public synchronized void removePartialMessage(Player player) {
        partialMessages.remove(player);
    }
}
