package com.kierdavis.kmail;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KMail extends JavaPlugin {
    public Map<Player, PartialMessage> partialMessages;
    
    public void onEnable() {
        partialMessages = new HashMap<Player, PartialMessage>();
        
        getCommand("kmail").setExecutor(new KMailCommandExecutor(this));
    }
    
    public void onDisable() {
        
    }
    
    public String getLocalHostname() {
        return "local.fix.this.net";
    }
}
