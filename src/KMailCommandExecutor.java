package com.kierdavis.kmail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KMailCommandExecutor implements CommandExecutor {
    public static final int ITEMS_PER_PAGE = 10;
    
    private KMail plugin;
    private Map<CommandSender, Message> selected;
    
    public KMailCommandExecutor(KMail plugin) {
        this.plugin = plugin;
        this.selected = new HashMap<CommandSender, Message>();
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
        
        if (subcmd.equalsIgnoreCase("list")) {
            return doList(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("select")) {
            return doSelect(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("read")) {
            return doRead(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("tag")) {
            return doTag(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("untag")) {
            return doUntag(sender, args);
        }
        
        if (subcmd.equalsIgnoreCase("delete")) {
            return doDelete(sender, args);
        }
        
        sender.sendMessage("Invalid subcommand: " + subcmd);
        return false;
    }
    
    private boolean doHelp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.help")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.help)");
            return false;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "KMail Help: (" + ChatColor.RED + "<required> [optional]" + ChatColor.YELLOW + ")");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail send " + ChatColor.RED + "<address> [message]");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail list " + ChatColor.RED + "[criteria] [page]");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail select " + ChatColor.RED + "<id>");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail read " + ChatColor.RED + "[id]");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail read next");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail tag " + ChatColor.RED + "[id] <tags...>");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail untag " + ChatColor.RED + "[id] <tags...>");
            sender.sendMessage(ChatColor.DARK_RED + "  /kmail delete " + ChatColor.RED + "[id]");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "Do " + ChatColor.DARK_RED + "/kmail help " + ChatColor.RED + "<command>" + ChatColor.YELLOW + " for help on any subcommand.");
            sender.sendMessage(ChatColor.YELLOW + "Other help topics: addresses");
            return true;
        }
        
        String topic = args[0];
        
        if (topic.equalsIgnoreCase("send")) {
            sender.sendMessage(ChatColor.DARK_RED + "/kmail send " + ChatColor.RED + "<address> [message]");
            sender.sendMessage(ChatColor.YELLOW + "Send a message to the specified address. If the message is not specified in the command, all future chat messages (until one consisting of a single period ('.') is sent) will be used as the body of the message.");
            sender.sendMessage(ChatColor.YELLOW + "See also: " + ChatColor.DARK_RED + "/kmail help addresses");
            return true;
        }
        
        if (topic.equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.DARK_RED + "/kmail list " + ChatColor.RED + "[criteria] [page]");
            sender.sendMessage(ChatColor.YELLOW + "Lists messages with the given criteria.");
            sender.sendMessage(ChatColor.YELLOW + "See also: " + ChatColor.DARK_RED + "/kmail help criteria");
            return true;
        }
        
        if (topic.equalsIgnoreCase("select")) {
            sender.sendMessage(ChatColor.DARK_RED + "/kmail select " + ChatColor.RED + "[id]");
            sender.sendMessage(ChatColor.YELLOW + "Selects a message identified by its local ID (the number at the start of each line output by " + ChatColor.DARK_RED + "/kmail list" + ChatColor.YELLOW + ").");
            return true;
        }
        
        if (topic.equalsIgnoreCase("read")) {
            sender.sendMessage(ChatColor.DARK_RED + "/kmail read " + ChatColor.RED + "[id]");
            sender.sendMessage(ChatColor.YELLOW + "Displays a message identified by its local ID (or the selected message if omitted) and marks it as read.");
            sender.sendMessage(ChatColor.DARK_RED + "/kmail read next");
            sender.sendMessage(ChatColor.YELLOW + "Displays the first unread message and marks it as read.");
            return true;
        }
        
        if (topic.equalsIgnoreCase("tag")) {
            sender.sendMessage(ChatColor.DARK_RED + "/kmail tag " + ChatColor.RED + "[id] <tags...>");
            sender.sendMessage(ChatColor.YELLOW + "Adds the specified tags to the message identified by its local ID (or the selected message if omitted)");
            return true;
        }
        
        if (topic.equalsIgnoreCase("untag")) {
            sender.sendMessage(ChatColor.DARK_RED + "/kmail untag " + ChatColor.RED + "[id] <tags...>");
            sender.sendMessage(ChatColor.YELLOW + "Removes the specified tags from the message identified by its local ID (or the selected message if omitted)");
            return true;
        }
        
        if (topic.equalsIgnoreCase("delete")) {
            sender.sendMessage(ChatColor.DARK_RED + "/kmail delete " + ChatColor.RED + "[id]");
            sender.sendMessage(ChatColor.YELLOW + "Deletes a message identified by its local ID (or the selected message if omitted)");
            return true;
        }
        
        if (topic.equalsIgnoreCase("addresses")) {
            sender.sendMessage(ChatColor.YELLOW + "An address can be:");
            sender.sendMessage(ChatColor.GREEN + "  <username>");
            sender.sendMessage(ChatColor.YELLOW + "    - The username of a player on the local server");
            sender.sendMessage(ChatColor.GREEN + "  <username>" + ChatColor.DARK_GREEN + "@" + ChatColor.GREEN + "<server-addr>");
            sender.sendMessage(ChatColor.YELLOW + "    - A user on another server (" + ChatColor.GREEN + "<server-addr>" + ChatColor.YELLOW + " is the same IP address/domain name used to connect to it from the Minecraft client)");
            return true;
        }
        
        if (topic.equalsIgnoreCase("criteria")) {
            sender.sendMessage(ChatColor.YELLOW + "Search criteria are space-seperated values that can take the form of:");
            sender.sendMessage(ChatColor.DARK_GREEN + "  t:" + ChatColor.GREEN + "<tag>");
            sender.sendMessage(ChatColor.YELLOW + "    Messages with a certain tag. Predefined tags are: 'unread'");
            return true;
        }
        
        sender.sendMessage("Invalid help topic: " + topic);
        return false;
    }
    
    private boolean doSend(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.send")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.send)");
            return false;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.DARK_RED + "/kmail send " + ChatColor.RED + "<address> [message]");
            sender.sendMessage(ChatColor.YELLOW + "See " + ChatColor.DARK_RED + "/kmail help send" + ChatColor.YELLOW + " for more info.");
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
        
        if (destAddress.getHostname() != "local" && !sender.hasPermission("kmail.send.remote")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.send.remote)");
            return false;
        }
        
        if (args.length >= 2) {
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append(args[1]);
            
            for (int i = 2; i < args.length; i++) {
                bodyBuilder.append(" ").append(args[i]);
            }
            
            msg.setBody(bodyBuilder.toString());
            plugin.sendMessage(msg);
            
            sender.sendMessage(ChatColor.YELLOW + "Mail queued.");
            
            return true;
        }
        
        else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.YELLOW + "This command must be run as a player.");
                return false;
            }
            
            Player player = (Player) sender;
            PartialMessage pm = new PartialMessage(msg);
            
            plugin.putPartialMessage(player, pm);
            
            sender.sendMessage(ChatColor.YELLOW + "Now type your mail message in normal chat (not with commands).");
            sender.sendMessage(ChatColor.YELLOW + "End the message with a single chat message containing a dot (period).");
            
            return true;
        }
    }
    
    private boolean doList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.list")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.list)");
            return false;
        }
        
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
        
        sender.sendMessage(ChatColor.YELLOW + "Messages (page " + ChatColor.GREEN + pageNum + ChatColor.YELLOW + "):");
        
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
            sender.sendMessage(ChatColor.YELLOW + "  No messages.");
        }
        
        return true;
    }
    
    private boolean doSelect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.select")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.select)");
            return false;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.DARK_RED + "/kmail select " + ChatColor.RED + "<id>");
            sender.sendMessage(ChatColor.YELLOW + "See " + ChatColor.DARK_RED + "/kmail help select" + ChatColor.YELLOW + " for more info.");
            return false;
        }
        
        long id;
        try {
            id = Long.parseLong(args[0]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.YELLOW + "Bad number format.");
            sender.sendMessage(ChatColor.YELLOW + "See " + ChatColor.DARK_RED + "/kmail help select" + ChatColor.YELLOW + " for more info.");
            return false;
        }
        
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Message msg = mb.getByID(id);
        
        if (msg == null) {
            sender.sendMessage(ChatColor.YELLOW + "No message with that ID in your mailbox.");
            return false;
        }
        
        selected.put(sender, msg);
        sender.sendMessage(ChatColor.YELLOW + "Message selected.");
        
        return true;
    }
    
    private boolean doRead(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.read")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.read)");
            return false;
        }
        
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Message msg;
        
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("next")) {
                Iterator<Message> it = mb.searchTag("unread");
                if (it.hasNext()) {
                    msg = it.next();
                }
                else {
                    sender.sendMessage(ChatColor.YELLOW + "No unread messages.");
                    return false;
                }
            }
            
            else {
                long id;
                try {
                    id = Long.parseLong(args[0]);
                }
                catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.YELLOW + "Bad number format.");
                    sender.sendMessage(ChatColor.YELLOW + "See " + ChatColor.DARK_RED + "/kmail help read" + ChatColor.YELLOW + " for more info.");
                    return false;
                }
        
                msg = mb.getByID(id);
                if (msg == null) {
                    sender.sendMessage(ChatColor.YELLOW + "No message with that ID in your mailbox.");
                    return false;
                }
            }
        }
        
        else {
            msg = selected.get(sender);
            if (msg == null) {
                sender.sendMessage(ChatColor.YELLOW + "No message selected.");
                return false;
            }
        }
        
        if (msg.isUnread()) {
            msg.markRead();
        }
        
        displayMessage(sender, msg);
        
        return true;
    }
    
    private boolean doTag(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.tag")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.tag)");
            return false;
        }
        
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Message msg = null;
        
        if (args.length >= 1) {
            try {
                long id = Long.parseLong(args[0]);
                msg = mb.getByID(id);
                if (msg == null) {
                    sender.sendMessage(ChatColor.YELLOW + "No message with that ID in your mailbox.");
                    return false;
                }
                
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            
            catch (NumberFormatException e) {}
        }
        
        if (msg == null) {
            msg = selected.get(sender);
            if (msg == null) {
                sender.sendMessage(ChatColor.YELLOW + "No message selected.");
                return false;
            }
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.DARK_RED + "/kmail tag " + ChatColor.RED + "[id] <tags...>");
            sender.sendMessage(ChatColor.YELLOW + "See " + ChatColor.DARK_RED + "/kmail help tag" + ChatColor.YELLOW + " for more info.");
            return false;
        }
        
        for (int i = 0; i < args.length; i++) {
            msg.addTag(args[i]);
        }
        
        sender.sendMessage(ChatColor.YELLOW + Integer.toString(args.length) + " tags added.");
        
        return true;
    }
    
    private boolean doUntag(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.tag")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.tag)");
            return false;
        }
        
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Message msg = null;
        
        if (args.length >= 1) {
            try {
                long id = Long.parseLong(args[0]);
                msg = mb.getByID(id);
                if (msg == null) {
                    sender.sendMessage(ChatColor.YELLOW + "No message with that ID in your mailbox.");
                    return false;
                }
                
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            
            catch (NumberFormatException e) {}
        }
        
        if (msg == null) {
            msg = selected.get(sender);
            if (msg == null) {
                sender.sendMessage(ChatColor.YELLOW + "No message selected.");
                return false;
            }
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.DARK_RED + "/kmail untag " + ChatColor.RED + "[id] <tags...>");
            sender.sendMessage(ChatColor.YELLOW + "See " + ChatColor.DARK_RED + "/kmail help untag" + ChatColor.YELLOW + " for more info.");
            return false;
        }
        
        for (int i = 0; i < args.length; i++) {
            msg.removeTag(args[i]);
        }
        
        sender.sendMessage(ChatColor.YELLOW + Integer.toString(args.length) + " tags removed.");
        
        return true;
    }
    
    private boolean doDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.delete")) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have the required permission (kmail.delete)");
            return false;
        }
        
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Message msg = null;
        
        if (args.length >= 1) {
            try {
                long id = Long.parseLong(args[0]);
                msg = mb.getByID(id);
                if (msg == null) {
                    sender.sendMessage(ChatColor.YELLOW + "No message with that ID in your mailbox.");
                    return false;
                }
                
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            
            catch (NumberFormatException e) {}
        }
        
        if (msg == null) {
            msg = selected.get(sender);
            if (msg == null) {
                sender.sendMessage(ChatColor.YELLOW + "No message selected.");
                return false;
            }
        }
        
        mb.remove(msg);
        sender.sendMessage(ChatColor.YELLOW + "Message deleted.");
        
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
        String tagStr = getTagStr(msg);
        
        sender.sendMessage(ChatColor.YELLOW + "================================");
        sender.sendMessage(ChatColor.YELLOW + "From: " + ChatColor.GREEN + msg.getSrcAddress().toString());
        sender.sendMessage(ChatColor.YELLOW + "To: " + ChatColor.GREEN + msg.getDestAddress().toString());
        sender.sendMessage(ChatColor.YELLOW + "Sent: " + ChatColor.RED + msg.getSentDate().toString());
        sender.sendMessage(ChatColor.YELLOW + "Recieved: " + ChatColor.RED + msg.getReceivedDate().toString());
        sender.sendMessage(ChatColor.YELLOW + "Tags: " + tagStr);
        sender.sendMessage("");
        sender.sendMessage(msg.getBody());
        sender.sendMessage(ChatColor.YELLOW + "================================");
    }
    
    private void displayMessageSummary(CommandSender sender, Message msg) {
        String bodySummary = msg.getBody();
        
        if (bodySummary.length() > 18) {
            bodySummary = bodySummary.substring(15) + "...";
        }
        
        StringBuilder b = new StringBuilder();
        
        if (selected.get(sender) == msg) {
            b.append(ChatColor.DARK_AQUA + "->");
        }
        else {
            b.append("   ");
        }
        
        b.append(ChatColor.RED);
        b.append(msg.getLocalID());
        
        if (!msg.isRead()) {
            b.append(ChatColor.PINK + " [unread]");
        }
        
        b.append(ChatColor.GREEN + " ");
        b.append(msg.getSrcAddress().toString());
        b.append(ChatColor.YELLOW + ": ");
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
    
    public String getTagStr(Message msg) {
        Iterator<String> it = msg.getTags().iterator();
        if (!it.hasNext()) {
            return "None";
        }
        
        StringBuilder b = new StringBuilder();
        b.append(ChatColor.GREEN).append((String) it.next());
        
        while (it.hasNext()) {
            b.append(ChatColor.YELLOW + ", " + ChatColor.GREEN).append((String) it.next());
        }
        
        return b.toString();
    }
}
