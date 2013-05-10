package com.kierdavis.kmail;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.ChatColor;
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
        
        // Preload mailboxes of all online players
        Player[] players = getServer().getOnlinePlayers();
        getLogger().info("Preloading " + Integer.toString(players.length) + " mailboxes");
        for (int i = 0; i < players.length; i++) {
            getMailbox(players[i].getName(), true);
        }
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Register commands
        getCommand("kmail").setExecutor(new KMailCommandExecutor(this));
        
        // Start our threads
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
        
        // Save mailboxes
        getLogger().info("Saving " + Integer.toString(mailboxes.size()) + " mailboxes");
        Iterator<String> it = mailboxes.keySet().iterator();
        
        while (it.hasNext()) {
            String username = (String) it.next();
            saveMailbox(username);
        }
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
    
    public Mailbox getMailbox(String username, boolean create) {
        Mailbox mb = mailboxes.get(username.toLowerCase());
        
        if (mb == null) {
            mb = Mailbox.load(this, username);
            if (mb == null) {
                if (create) {
                    mb = new Mailbox();
                }
                else {
                    return null;
                }
            }
            
            mailboxes.put(username.toLowerCase(), mb);
        }
        
        return mb;
    }
    
    public void saveMailbox(String username) {
        try {
            getMailbox(username, true).save(this, username);
        }
        catch (IOException e) {
            getLogger().info("Could not save mailbox: " + e.toString());
        }
    }
    
    public void receiveMessage(Message msg) {
        MailDeliverEvent event = new MailDeliverEvent(msg);
        getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        
        String username = msg.getDestAddress().getUsername();
        Mailbox mb = getMailbox(username, false);
        
        if (mb == null) {
            msg = errorResponse(msg, "No user named '" + username + "' at destination host.");
            sendMessage(msg);
            return;
        }
        
        mb.receive(msg);
        
        if (username.equalsIgnoreCase("CONSOLE")) {
            getLogger().info(ChatColor.YELLOW + "Incoming mail from " + ChatColor.GREEN + "" + msg.getSrcAddress().toString() + ChatColor.YELLOW + ".");
            getLogger().info(ChatColor.YELLOW + "Type " + ChatColor.DARK_RED + "kmail read next" + ChatColor.YELLOW + " to begin reading unread mail.");
        }
        
        else {
            Player player = getServer().getPlayer(username);
            if (player != null) {
                player.sendMessage(ChatColor.YELLOW + "Incoming mail from " + ChatColor.GREEN + "" + msg.getSrcAddress().toString() + ChatColor.YELLOW + ".");
                player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.DARK_RED + "/kmail read next" + ChatColor.YELLOW + " to begin reading unread mail.");
            }
        }
        
        saveMailbox(username);
    }
    
    public synchronized void sendMessage(Message msg) {
        if (msg.getSrcAddress().getHostname().equals("local")) {
            msg.getSrcAddress().setHostname(getLocalHostname());
        }
        if (msg.getDestAddress().getHostname().equals("local")) {
            msg.getDestAddress().setHostname(getLocalHostname());
        }
        
        if (msg.getSentDate() == null) {
            msg.setSentDate(new Date());
        }
        
        MailSendEvent event = new MailSendEvent(msg);
        getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        
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
    
    public Message errorResponse(Message cause, String error) {
        Message msg = new Message();
        msg.setSrcAddress(new Address("KMail", getLocalHostname()));
        msg.setDestAddress(cause.getSrcAddress());
        
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Sending of the following message failed because: ").append(error);
        bodyBuilder.append("  From: ").append(cause.getSrcAddress().toString()).append("\n");
        bodyBuilder.append("  To: ").append(cause.getDestAddress().toString()).append("\n");
        bodyBuilder.append("  Sent: ").append(cause.getSentDate().toString()).append("\n\n");
        bodyBuilder.append(cause.getBody());
        msg.setBody(bodyBuilder.toString());
        
        return msg;
    }
}
