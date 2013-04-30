package com.kierdavis.kmail;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KMail extends JavaPlugin {
    private Map<Player, PartialMessage> partialMessages;
    
    public void onEnable() {
        partialMessages = new HashMap<Player, PartialMessage>();
        
        new PartialMessageListener(this);
        
        getCommand("kmail").setExecutor(new KMailCommandExecutor(this));
    }
    
    public void onDisable() {
        
    }
    
    public String getLocalHostname() {
        return "local.fix.this.net";
    }
    
    public void queueMessage(Message msg) {
        getLogger().debug("Mail to be queued:");
        getLogger().debug("  From: " + msg.getSrcAddress().toString());
        getLogger().debug("  To: " + msg.getDestAddress().toString());
        getLogger().debug("  Body: " + msg.getBody());
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
