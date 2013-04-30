package com.kierdavis.kmail;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PartialMessageListener implements Listener {
    private KMail plugin;
    
    public PartialMessageListener(KMail plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        String s = event.getMessage();
        Player player = event.getPlayer();
        PartialMessage pm = plugin.getPartialMessage(player);
                
        if (pm != null) {
            if (s.equals(".")) {
                plugin.removePartialMessage(player);
                player.sendMessage("Message body ended.");
                
                Message msg = pm.finish();
                plugin.queueMessage(msg);
                player.sendMessage("Mail queued for delivery.");
            }
            
            else {
                player.sendMessage("Message body updated.");
                pm.append(s);
            }
        }
    }
}
