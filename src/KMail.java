package com.kierdavis.kmail;

import com.kierdavis.kmail.events.MailDeliverEvent;
import com.kierdavis.kmail.events.MailSendEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class KMail extends JavaPlugin {
    private Map<Player, PartialMessage> partialMessages;
    private Map<String, Mailbox> mailboxes;
    private WebServer server;
    private WebClient client;
    private MailDispatcher dispatcher;
    private QueuePoller poller;
    
    public void onEnable() {
        // Ensure config.yml exists
        saveDefaultConfig();
        
        // Initialise variables
        partialMessages = new HashMap<Player, PartialMessage>();
        mailboxes = new HashMap<String, Mailbox>();
        server = new WebServer(this);
        client = new WebClient(this);
        dispatcher = new MailDispatcher(this, client);
        poller = null;
        
        // Preload mailboxes of all online players
        loadMailboxes();
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Register commands
        getCommand("kmail").setExecutor(new KMailCommandExecutor(this));
        
        // Start our threads
        getLogger().info("Starting mail dispatcher");
        dispatcher.start();
        
        if (getQueues().size() > 0) {
            poller = new QueuePoller(this);
            
            getLogger().info("Starting queue poller");
            poller.start();
        }
        
        if (isServerEnabled()) {
            getLogger().info("Starting web server");
            try {
                server.start();
            }
            catch (IOException e) {
                getLogger().severe("Could not start web server: " + e.toString());
            }
        }
        
        // Start Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        }
        catch (IOException e) {
            getLogger().severe("Failed to submit stats to Metrics: " + e.toString());
        }
    }
    
    public void onDisable() {
        // Stop our threads
        getLogger().info("Stopping mail dispatcher");
        dispatcher.stop();
        
        if (poller != null) {
            getLogger().info("Stopping queue poller");
            poller.stop();
        }
        
        if (isServerEnabled()) {
            getLogger().info("Stopping web server");
            server.stop();
        }
        
        // Save mailboxes
        saveMailboxes();
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
    
    public boolean isServerEnabled() {
        return getConfig().getBoolean("remote.server.enabled", true);
    }
    
    public String getServerIP() {
        return getConfig().getString("remote.server.ip", getDefaultServerIP());
    }
    
    public int getServerPort() {
        return getConfig().getInt("remote.server.port", 4880);
    }
    
    public List<String> getQueues() {
        return getConfig().getStringList("remote.queues");
    }
    
    public ConfigurationSection getHostConfig(String name) {
        return getConfig().getConfigurationSection("hosts." + name);
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
    
    public void saveMailboxes() {
        getLogger().info("Saving " + Integer.toString(mailboxes.size()) + " mailboxes");
        Iterator<String> it = mailboxes.keySet().iterator();
        
        while (it.hasNext()) {
            String username = (String) it.next();
            saveMailbox(username);
        }
        
        mailboxes.clear();
    }
    
    public int numMailboxes() {
        return mailboxes.size();
    }
    
    public void reloadMailboxes() {
        mailboxes.clear();
        loadMailboxes();
    }
    
    public void loadMailboxes() {
        Player[] players = getServer().getOnlinePlayers();
        for (int i = 0; i < players.length; i++) {
            getMailbox(players[i].getName(), true);
        }
        
        getMailbox("CONSOLE", true);
    }
    
    public void receiveMessage(Message msg) {
        MailDeliverEvent event = new MailDeliverEvent(msg);
        getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        
        String username = msg.getDestAddress().getUsername();
        if (username.equals("*")) {
            receiveAll(msg);
            return;
        }
        
        Mailbox mb = getMailbox(username, false);
        
        if (mb == null) {
            msg = errorResponse(msg, "No user named '" + username + "' at destination host.");
            sendMessage(msg);
            return;
        }
        
        mb.receive(msg);
        saveMailbox(username);
        
        notifyReceiver(username, msg);
    }
    
    public synchronized void sendMessage(Message msg) {
        // Replace "local" with local hostname
        if (msg.getSrcAddress().getHostname().equals("local")) {
            msg.getSrcAddress().setHostname(getLocalHostname());
        }
        if (msg.getDestAddress().getHostname().equals("local")) {
            msg.getDestAddress().setHostname(getLocalHostname());
        }
        
        // Update sent date
        if (msg.getSentDate() == null) {
            msg.setSentDate(new Date());
        }
        
        // Update reply-via
        if (getQueues().size() > 0) {
            msg.setReplyVia(getQueues().get(0));
        }
        
        // Apply host config
        if (!msg.getDestAddress().getHostname().equals(getLocalHostname())) {
            ConfigurationSection cfg = getHostConfig(msg.getDestAddress().getHostname());
            if (cfg != null) {
                if (cfg.contains("via")) {
                    msg.setSendVia(cfg.getString("via"));
                }
            }
        }
        
        // Dispatch event
        MailSendEvent event = new MailSendEvent(msg);
        getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        
        // Queue message
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
        bodyBuilder.append("Sending of the following message failed because: ").append(error).append("\n");
        bodyBuilder.append("  From: ").append(cause.getSrcAddress().toString()).append("\n");
        bodyBuilder.append("  To: ").append(cause.getDestAddress().toString()).append("\n");
        bodyBuilder.append("  Sent: ").append(cause.getSentDate().toString()).append("\n\n");
        bodyBuilder.append(cause.getBody());
        msg.setBody(bodyBuilder.toString());
        
        return msg;
    }
    
    private void receiveAll(Message msg) {
        saveMailboxes(); // Make sure mailboxes folder contains newly created mailboxes too.
        
        File dir = new File(getDataFolder(), "mailboxes");
        File[] files = dir.listFiles();
        
        if (files == null) return;
        
        getLogger().info("Distributing incoming 'sendall' message to " + Integer.toString(files.length) + " recipients...");
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf('.'));
            
            Message msg1 = msg.clone();
            Mailbox mb = getMailbox(name, false);
            
            mb.receive(msg1);
            saveMailbox(name);
            notifyReceiver(name, msg1);
            
            if ((i % 100) == 99) {
                // Every 100 mailboxes, clear the cache.
                getLogger().info("Distribution progress: " + Integer.toString(i + 1) + "/" + Integer.toString(files.length));
                mailboxes.clear();
            }
        }
        
        // Clear cached mailboxes & reset from online players.
        getLogger().info("Distribution complete. Reloading mailboxes to clear cache.");
        reloadMailboxes();
    }
    
    public void notifyReceiver(String username, Message msg) {
        CommandSender sender = null;
        
        if (username.equalsIgnoreCase("CONSOLE")) {
            sender = getServer().getConsoleSender();
        }
        else {
            sender = getServer().getPlayer(username);
        }
        
        if (sender != null) {
            sender.sendMessage(ChatColor.YELLOW + "Incoming mail from " + ChatColor.GREEN + "" + msg.getSrcAddress().toString() + ChatColor.YELLOW + ".");
            sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.DARK_RED + "/kmail read next" + ChatColor.YELLOW + " to begin reading unread mail.");
        }
    }
    
    public void pollQueues() {
        List<String> queues = getQueues();
        for (int i = 0; i < queues.size(); i++) {
            pollQueue(queues.get(i));
        }
    }
    
    public void pollQueue(String addr) {
        List<Message> msgs = client.pollQueue(addr);
        
        if (msgs != null) {
            for (int i = 0; i < msgs.size(); i++) {
                receiveMessage(msgs.get(i));
            }
        }
    }
    
    public void triggerQueuePoller() {
        poller.runTask(this);
    }
}
