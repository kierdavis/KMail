package com.kierdavis.kmail;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DelayedJoinTask implements BukkitRunnable {
    private KMail plugin;
    private Player player;
    
    public DelayedJoinTask(KMail plugin_, Player player_) {
        plugin = plugin_;
        player = player_;
    }
    
    public void run() {
        if (!player.isOnline()) {
            return;
        }
        
        Mailbox mb = plugin.getMailbox(player.getName());
        int numUnread = mb.numUnread();
        
        if (numUnread > 0) {
            String pluralization = numUnread == 1 ? "" : "s";
            player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GREEN + Integer.toString(numUnread) + ChatColor.YELLOW + " unread message" + pluralization + ".");
            player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.DARK_RED + "/kmail read next" + ChatColor.YELLOW + " to begin reading them.");
        }
    }
}
