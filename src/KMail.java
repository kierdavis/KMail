package com.kierdavis.kmail;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KMail extends JavaPlugin {
    private Map<Player, PartialMessage> partialMessages;
    private Map<String, Mailbox> mailboxes;
    private MailDispatcher dispatcher;
    
    public void onEnable() {
        partialMessages = new HashMap<Player, PartialMessage>();
        mailboxes = new HashMap<String, Mailbox>();
        dispatcher = new MailDispatcher(this);
        
        new PartialMessageListener(this);
        
        getCommand("kmail").setExecutor(new KMailCommandExecutor(this));
        
        getLogger().info("Starting mail dispatcher");
        dispatcher.start();
    }
    
    public void onDisable() {
        getLogger().info("Stopping mail dispatcher");
        dispatcher.stop();
    }
    
    public String getLocalHostname() {
        return "local.fix.this.net";
    }
    
    public Mailbox getMailbox(String username) {
        Mailbox mb = mailboxes.get(username.toLowerCase());
        
        if (mb == null) {
            mb = new Mailbox();
            mailboxes.put(username, mb);
        }
        
        return mb;
    }
    
    public void receiveMessage(Message msg) {
        String username = msg.getDestAddress().getUsername();
        getMailbox(username).add(msg);
        
        if (username.equalsIgnoreCase("CONSOLE")) {
            getLogger().info("Incoming mail from " + msg.getSrcAddress().toString() + ".");
            getLogger().info("Type 'kmail read' to view it.");
        }
        
        else {
            Player player = getServer().getPlayer(username);
            if (player != null) {
                player.sendMessage("Incoming mail from " + msg.getSrcAddress().toString() + ".");
                player.sendMessage("Type '/kmail read' to view it.");
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
