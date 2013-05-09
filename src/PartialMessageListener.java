package com.kierdavis.kmail;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PartialMessageListener implements Listener {
    private KMail plugin;
    
    public PartialMessageListener(KMail plugin_) {
        plugin = plugin_;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        String s = event.getMessage();
        Player player = event.getPlayer();
        PartialMessage pm = plugin.getPartialMessage(player);
                
        if (pm != null) {
            if (s.equals(".")) {
                plugin.removePartialMessage(player);
                plugin.sendMessage(pm.finish());
                
                player.sendMessage("Mail queued.");
            }
            
            else {
                player.sendMessage("Message body updated.");
                pm.append(s);
            }
            
            event.setCancelled(true);
        }
    }
}
