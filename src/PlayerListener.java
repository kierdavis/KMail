package com.kierdavis.kmail;

import org.bukkit.entity.player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private KMail plugin;
    
    public PlayerListener(KMail plugin_) {
        plugin = plugin_;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        DelayedJoinTask task = new DelayedJoinTask(plugin, event.getPlayer());
        task.runTaskLater(plugin, 20);
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
