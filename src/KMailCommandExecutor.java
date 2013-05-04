package com.kierdavis.kmail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
        
        sender.sendMessage("Invalid subcommand: " + subcmd);
        return false;
    }
    
    private boolean doHelp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.help")) {
            sender.sendMessage("\247eYou don't have the required permission (kmail.help)");
            return false;
        }
        
        if (args.length < 1) {
            sender.sendMessage("\247eKMail Help: (\247c<required> [optional]\247e)");
            sender.sendMessage("  \2474/kmail send \247c<address> [message]");
            sender.sendMessage("  \2474/kmail list \247c[criteria] [page]");
            sender.sendMessage("  \2474/kmail select \247c<id>");
            sender.sendMessage("  \2474/kmail read \247c[id]");
            sender.sendMessage("");
            sender.sendMessage("\247eDo \2474/kmail help \247c<command>\247e for help on any subcommand.");
            sender.sendMessage("\247eOther help topics: addresses");
            return true;
        }
        
        String topic = args[0];
        
        if (topic.equalsIgnoreCase("send")) {
            sender.sendMessage("\2474/kmail send \247c<address> [message]");
            sender.sendMessage("\247eSend a message to the specified address. If the message is not specified in the command, all future chat messages (until one consisting of a single period ('.') is sent) will be used as the body of the message.");
            sender.sendMessage("\247eSee also: \2474/kmail help addresses");
            return true;
        }
        
        if (topic.equalsIgnoreCase("list")) {
            sender.sendMessage("\2474/kmail list \247c[criteria] [page]");
            sender.sendMessage("\247eLists messages with the given criteria.");
            sender.sendMessage("\247eSee also: \2474/kmail help criteria");
            return true;
        }
        
        if (topic.equalsIgnoreCase("select")) {
            sender.sendMessage("\2474/kmail select \247c[id]");
            sender.sendMessage("\247eSelects a message identified by its local ID (the number at the start of each line output by \2474/kmail list\247e).");
            return true;
        }
        
        if (topic.equalsIgnoreCase("read")) {
            sender.sendMessage("\2474/kmail read \247c[id]");
            sender.sendMessage("\247eDisplays a message identified by its local ID (or the selected message if omitted) and marks it as read.");
            return true;
        }
        
        if (topic.equalsIgnoreCase("addresses")) {
            sender.sendMessage("\247eAn address can be:");
            sender.sendMessage("  \247a<username>");
            sender.sendMessage("    \247e- The username of a player on the local server");
            sender.sendMessage("  \247a<username>\2472@\247a<server-addr>");
            sender.sendMessage("    \247e- A user on another server (\247a<server-addr>\247e is the same IP address/domain name used to connect to it from the Minecraft client)");
            return true;
        }
        
        if (topic.equalsIgnoreCase("criteria")) {
            sender.sendMessage("\247eSearch criteria are space-seperated values that can take the form of:");
            sender.sendMessage("  \2472t:\247a<tag>");
            sender.sendMessage("    \247eMessages with a certain tag. Predefined tags are: 'unread'");
            return true;
        }
        
        sender.sendMessage("Invalid help topic: " + topic);
        return false;
    }
    
    private boolean doSend(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.send")) {
            sender.sendMessage("\247eYou don't have the required permission (kmail.send)");
            return false;
        }
        
        if (args.length < 1) {
            sender.sendMessage("\247eUsage: \2474/kmail send \247c<address> [message]");
            sender.sendMessage("\247eSee \2474/kmail help send\247e for more info.");
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
            sender.sendMessage("\247eYou don't have the required permission (kmail.send.remote)");
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
            
            sender.sendMessage("\247eMail queued.");
            
            return true;
        }
        
        else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\247eThis command must be run as a player.");
                return false;
            }
            
            Player player = (Player) sender;
            PartialMessage pm = new PartialMessage(msg);
            
            plugin.putPartialMessage(player, pm);
            
            sender.sendMessage("\247eNow type your mail message in normal chat (not with commands).");
            sender.sendMessage("\247eEnd the message with a single chat message containing a dot (period).");
            
            return true;
        }
    }
    
    private boolean doList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.list")) {
            sender.sendMessage("\247eYou don't have the required permission (kmail.list)");
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
        
        sender.sendMessage("\247eMessages (page \247a" + pageNum + "\247e):");
        
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
            sender.sendMessage("  \247eNo messages.");
        }
        
        return true;
    }
    
    private boolean doSelect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.select")) {
            sender.sendMessage("\247eYou don't have the required permission (kmail.select)");
            return false;
        }
        
        if (args.length < 1) {
            sender.sendMessage("\247eUsage: \2474/kmail select \247c<id>");
            sender.sendMessage("\247eSee \2474/kmail help select\247e for more info.");
            return false;
        }
        
        long id = Long.parseLong(args[0]);
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Message msg = mb.getByID(id);
        
        if (msg == null) {
            sender.sendMessage("\247eNo message with that ID in your mailbox.");
            return false;
        }
        
        selected.put(sender, msg);
        sender.sendMessage("\247eMessage selected.");
        
        return true;
    }
    
    private boolean doRead(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmail.read")) {
            sender.sendMessage("\247eYou don't have the required permission (kmail.read)");
            return false;
        }
        
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Message msg;
        
        if (args.length >= 1) {
            long id = Long.parseLong(args[0]);
            msg = mb.getByID(id);
            
            if (msg == null) {
                sender.sendMessage("\247eNo message with that ID in your mailbox.");
                return false;
            }
        }
        
        else {
            msg = selected.get(sender);
            if (msg == null) {
                sender.sendMessage("\247eNo message selected.");
                return false;
            }
        }
        
        if (msg.isUnread()) {
            msg.markRead();
        }
        
        displayMessage(sender, msg);
        
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
        
        sender.sendMessage("\247e================================");
        sender.sendMessage("\247eFrom: \247a" + msg.getSrcAddress().toString());
        sender.sendMessage("\247eTo: \247a" + msg.getDestAddress().toString());
        sender.sendMessage("\247eSent: \247c" + msg.getSentDate().toString());
        sender.sendMessage("\247eRecieved: \247c" + msg.getReceivedDate().toString());
        sender.sendMessage("\247eTags: " + tagStr);
        sender.sendMessage("");
        sender.sendMessage(msg.getBody());
        sender.sendMessage("\247e================================");
    }
    
    private void displayMessageSummary(CommandSender sender, Message msg) {
        String bodySummary = msg.getBody();
        
        if (bodySummary.length() > 18) {
            bodySummary = bodySummary.substring(15) + "...";
        }
        
        StringBuilder b = new StringBuilder();
        
        if (selected.get(sender) == msg) {
            b.append("\2473->");
        }
        else {
            b.append("   ");
        }
        
        b.append("\247c");
        b.append(msg.getLocalID());
        
        if (!msg.isRead()) {
            b.append("\247d [unread]");
        }
        
        b.append("\247a ");
        b.append(msg.getSrcAddress().toString());
        b.append("\247e: ");
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
        b.append("\247a").append((String) it.next());
        
        while (it.hasNext()) {
            b.append("\247e, \247a").append((String) it.next());
        }
        
        return b.toString();
    }
}
