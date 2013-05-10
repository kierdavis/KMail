package com.kierdavis.kmail;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        Mailbox mb = plugin.getMailbox(player.getName());
        int numUnread = mb.numUnread();
        
        if (numUnread > 0) {
            String pluralization = numUnread == 1 ? "" : "s";
            player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GREEN + Integer.toString(numUnread) + ChatColor.YELLOW + " unread message" + pluralization + ".");
            player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.DARK_RED + "/kmail read next" + ChatColor.YELLOW + " to begin reading them.");
        }
    }
}
