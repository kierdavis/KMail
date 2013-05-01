package com.kierdavis.kmail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KMailCommandExecutor implements CommandExecutor {
    public static final int ITEMS_PER_PAGE = 10;
    
    private KMail plugin;
    
    public KMailCommandExecutor(KMail plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            return doHelp(sender, args);
        }
        
        String subcmd = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        
        if (subcmd.equalsIgnoreCase("help")) {
            return doHelp(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("send")) {
            return doSend(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("read")) {
            return doRead(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("list")) {
            return doList(sender, args);
        }
        
        sender.sendMessage("Invalid subcommand: " + subcmd);
        return false;
    }
    
    private boolean doHelp(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("\xa7eKMail Help: (\xa7c<required> [optional]\xa7e)");
            sender.sendMessage("  \xa74/kmail send \xa7c<address> [message]");
            sender.sendMessage("  \xa74/kmail read");
            sender.sendMessage("");
            sender.sendMessage("\xa7eDo \xa74/kmail help \xa7c<command>\xa7e for help on any subcommand.");
            sender.sendMessage("\xa7eOther help topics: addresses");
            return true;
        }
        
        String topic = args[0];
        
        if (topic.equalsIgnoreCase("send")) {
            sender.sendMessage("\xa74/kmail send \xa7c<address> [message]");
            sender.sendMessage("\xa7eSend a message to the specified address. If the message is not specified in the command, all future chat messages (until one consisting of a single period ('.') is sent) will be used as the body of the message.");
            sender.sendMessage("\xa7eSee also: \xa74/kmail help addresses");
            return true;
        }
        
        if (topic.equalsIgnoreCase("read")) {
            sender.sendMessage("\xa74/kmail read");
            sender.sendMessage("\xa7eDisplays the oldest unread message and marks it as read.");
            return true;
        }
        
        if (topic.equalsIgnoreCase("list")) {
            sender.sendMessage("\xa74/kmail list \xa7c[criteria] [page]");
            sender.sendMessage("\xa7eLists messages with the given criteria.");
            sender.sendMessage("\xa7eSee also: \xa74/kmail help criteria");
            return true;
        }
        
        if (topic.equalsIgnoreCase("addresses")) {
            sender.sendMessage("\xa7eAn address can be:");
            sender.sendMessage("  \xa7a<username>");
            sender.sendMessage("    \xa7e- The username of a player on the local server");
            sender.sendMessage("  \xa7a<username>\xa72@\xa7a<server-addr>");
            sender.sendMessage("    \xa7e- A user on another server (\xa7a<server-addr>\xa7e is the same IP address/domain name used to connect to it from the Minecraft client)");
            sender.sendMessage("  \xa72*");
            sender.sendMessage("    \xa7e- All players on the local server");
            return true;
        }
        
        if (topic.equalsIgnoreCase("criteria")) {
            sender.sendMessage("\xa7eSearch criteria are space-seperated values that can take the form of:");
            sender.sendMessage("  \xa72t:\xa7a<tag>");
            sender.sendMessage("    \xa7eMessages with a certain tag. Predefined tags are: 'unread'");
            return true;
        }
        
        sender.sendMessage("Invalid help topic: " + topic);
        return false;
    }
    
    private boolean doSend(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("\xa7eUsage: \xa74/kmail send \xa7c<address> [message]");
            sender.sendMessage("\xa7eSee \xa74/kmail help send\xa7e for more info.");
            return false;
        }
        
        String srcUsername;
        
        if (sender instanceof Player) {
            srcUsername = ((Player) sender).getName();
        }
        else {
            srcUsername = "CONSOLE";
        }
        
        Address srcAddress = new Address(srcUsername, "local");
        Address destAddress = new Address(args[0]);
        Message msg = new Message(srcAddress, destAddress);
        
        if (args.length >= 2) {
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append(args[1]);
            
            for (int i = 2; i < args.length; i++) {
                bodyBuilder.append(" ").append(args[i]);
            }
            
            msg.setBody(bodyBuilder.toString());
            plugin.sendMessage(msg);
            
            sender.sendMessage("\xa7eMail queued.");
            
            return true;
        }
        
        else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\xa7eThis command must be run as a player.");
                return false;
            }
            
            Player player = (Player) sender;
            PartialMessage pm = new PartialMessage(msg);
            
            plugin.putPartialMessage(player, pm);
            
            sender.sendMessage("\xa7eNow type your mail message in normal chat (not with commands).");
            sender.sendMessage("\xa7eEnd the message with a single chat message containing a dot (period).");
            
            return true;
        }
    }
    
    private boolean doRead(CommandSender sender, String[] args) {
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Iterator it = mb.iterator();
        
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            
            if (msg.isUnread()) {
                displayMessage(sender, msg);
                msg.markRead();
                return true;
            }
        }
        
        sender.sendMessage("\xa7eNo unread messages.");
        return true;
    }
    
    private boolean doList(CommandSender sender, String[] args) {
        Set<SearchCriteria> criteria = new HashSet<SearchCriteria>();
        
        int lastArgIndex = args.length - 1;
        int pageNum = 1;
        
        if (lastArgIndex >= 0) {
            try {
                pageNum = Integer.parseInt(args[lastArgIndex]);
                args = Arrays.copyOfRange(args, 0, lastArgIndex);
            }
            catch (NumberFormatException e) {
                pageNum = 1;
            }
        }
        
        int startIndex = (pageNum - 1) * ITEMS_PER_PAGE;
        
        for (int i = 0; i < args.length; i++) {
            SearchCriteria crit = parseSearchCriteria(args[i]);
            if (crit != null) {
                criteria.add(crit);
            }
        }
        
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Iterator<Message> it = mb.search(criteria);
        
        int i = 0;
        boolean itemsPrinted = false;
        
        sender.sendMessage("\xa7eMessages (page \xa7a" + pageNum + "\xa7e):");
        
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            
            if (i >= startIndex + ITEMS_PER_PAGE) {
                break;
            }
            
            if (i >= startIndex) {
                displayMessageSummary(sender, msg);
                itemsPrinted = true;
            }
            
            i++;
        }
        
        if (!itemsPrinted) {
            sender.sendMessage("  \xa7eNo messages.");
        }
        
        return true;
    }
    
    private String getUsername(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getName();
        }
        else {
            return "CONSOLE";
        }
    }
    
    private void displayMessage(CommandSender sender, Message msg) {
        sender.sendMessage("\xa7e================================");
        sender.sendMessage("\xa7eFrom: \xa7a" + msg.getSrcAddress().toString());
        sender.sendMessage("\xa7eTo: \xa7a" + msg.getDestAddress().toString());
        sender.sendMessage("\xa7eSent: \xa7c" + msg.getSentDate().toString());
        sender.sendMessage("\xa7eRecieved: \xa7c" + msg.getReceivedDate().toString());
        sender.sendMessage("");
        sender.sendMessage(msg.getBody());
        sender.sendMessage("\xa7e================================");
    }
    
    private void displayMessageSummary(CommandSender sender, Message msg) {
        String bodySummary = msg.getBody();
        
        if (bodySummary.length() > 18) {
            bodySummary = bodySummary.substring(15) + "...";
        }
        
        StringBuilder b = new StringBuilder();
        
        b.append("  \xa7c");
        b.append(msg.getLocalID());
        
        if (!msg.isRead()) {
            b.append(" \xa7d[unread]");
        }
        
        b.append(" \xa7a");
        b.append(msg.getSrcAddress().toString());
        b.append(": \xa7e");
        b.append(bodySummary);
        
        sender.sendMessage(b.toString());
    }
    
    public SearchCriteria parseSearchCriteria(String s) {
        if (s.length() < 3 || s.charAt(1) != ':') {
            return null;
        }
        
        char c = s.charAt(0);
        String arg = s.substring(2);
        
        switch (c) {
        case 't':
            return new TagSearchCriteria(arg);
        default:
            return null;
        }
    }
}
