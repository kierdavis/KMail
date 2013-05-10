package com.kierdavis.kmail;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DelayedJoinTask extends BukkitRunnable {
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
            String s1 = numUnread == 1 ? "" : "s";
            String s2 = numUnread == 1 ? "it" : "them";
            player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GREEN + Integer.toString(numUnread) + ChatColor.YELLOW + " unread message" + s1 + ".");
            player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.DARK_RED + "/kmail read next" + ChatColor.YELLOW + " to begin reading " + s2 + ".");
        }
        
        plugin.saveMailbox(player.getName());
    }
}
