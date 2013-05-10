package com.kierdavis.kmail;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginNotifierListener implements Listener {
    private KMail plugin;
    
    public LoginNotifierListener(KMail plugin_) {
        plugin = plugin_;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.runTaskAsynchronously(new DelayedJoinHandler(plugin, event.getPlayer()));
    }
}
