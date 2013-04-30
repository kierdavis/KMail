package com.kierdavis.kmail;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KMail extends JavaPlugin {
    private Map<Player, PartialMessage> partialMessages;
    private Map<Player, Mailbox> playerMailboxes;
    private Mailbox consoleMailbox;
    private MailDispatcher dispatcher;
    
    public void onEnable() {
        partialMessages = new HashMap<Player, PartialMessage>();
        playerMailboxes = new HashMap<Player, Mailbox>();
        consoleMailbox = new Mailbox();
        dispatcher = new MailDispatcher(this);
        
        new PartialMessageListener(this);
        
        getCommand("kmail").setExecutor(new KMailCommandExecutor(this));
    }
    
    public void onDisable() {
        
    }
    
    public String getLocalHostname() {
        return "local.fix.this.net";
    }
    
    public Mailbox getMailbox(String username) {
        username = username.toLowerCase();
        
        if (username.equals("console")) {
            return consoleMailbox;
        }
        
        else {
            Mailbox mb = playerMailboxes.get(username);
            
            if (mb == null) {
                mb = new Mailbox();
                playerMailboxes.put(username, mb);
            }
            
            return mb;
        }
    }
    
    public void receiveMessage(Message msg) {
        String username = msg.getDestAddress().getUsername();
        getMailbox(username).add(msg);
        
        if (username.equalsIgnoreCase("CONSOLE")) {
            getLogger().info("Incoming mail from " + mb.getSrcAddress().toString() + ".");
            getLogger().info("Type 'kmail read' to view it.");
        }
        
        else {
            Player player = server.getPlayer(username);
            if (player != null) {
                player.sendMessage("Incoming mail from " + mb.getSrcAddress().toString() + ".");
                player.sendMessage("Type '/kmail read' to view it.");
            }
        }
    }
    
    public synchronized void sendMessage(Message msg) {
        if (msg.getSrcAddress().getHostName().equals("local")) {
            msg.getSrcAddress().setHostName(getLocalHostname());
        }
        if (msg.getDestAddress().getHostName().equals("local")) {
            msg.getDestAddress().setHostName(getLocalHostname());
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
