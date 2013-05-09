package com.kierdavis.kmail;

import java.util.Date;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

public class EssentialsImporter implements Importer {
    public void importMail(KMail plugin, CommandSender sender, String[] args) {
        Essentials ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (ess == null) {
            sender.sendMessage(ChatColor.YELLOW + "The Essentials plugin is not enabled or could not be found.");
            return;
        }
        
        UserMap usermap = ess.getUserMap();
        Iterator<String> it = usermap.getAllUniqueUsers().iterator();
        int i = 0;
        
        while (it.hasNext()) {
            String username = (String) it.next();
            User user = usermap.getUser(username);
            
            if (user != null) {
                importUser(plugin, user);
                i++;
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + Integer.toString(i) + ChatColor.YELLOW + " mailboxes imported.");
    }
    
    public void importUser(KMail plugin, User user) {
        Mailbox mb = plugin.getMailbox(user.getName());
        Iterator<String> mailIt = user.getMails().iterator();
        String localHostname = plugin.getLocalHostname();
        int i = 0;
        
        while (mailIt.hasNext()) {
            Message msg = parseMail(localHostname, user.getName(), (String) mailIt.next());
            mb.receive(msg);
            i++;
        }
        
        if (i > 0) {
            user.sendMessage(ChatColor.GREEN + Integer.toString(i) + ChatColor.YELLOW + " messages were imported from Essentials into your KMail mailbox.");
            user.sendMessage(ChatColor.YELLOW + "Do " + ChatColor.DARK_RED + "/kmail read next" + ChatColor.YELLOW + " to read each one.");
        }
    }
    
    public Message parseMail(String localHostname, String username, String s) {
        Message msg = new Message();
        String sender;
        int colon = s.indexOf(":");
        
        if (colon < 0) {
            msg.setBody(s);
            
            sender = "KMail-Import";
        }
        
        else {
            msg.setBody(s.substring(colon + 1).trim());
        
            sender = s.substring(0, colon).trim();
            if (sender.equals("Server")) {
                sender = "CONSOLE";
            }
        }
        
        msg.setSrcAddress(new Address(sender, localHostname));
        msg.setDestAddress(new Address(username, localHostname));
        msg.setSentDate(new Date());
        msg.addTag("imported");
        
        return msg;
    }
}
