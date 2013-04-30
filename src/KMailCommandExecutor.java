package com.kierdavis.kmail;

import java.util.Arrays;
import java.util.Iterator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KMailCommandExecutor implements CommandExecutor {
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
            sender.sendMessage("KMail Help: (<required> [optional])");
            sender.sendMessage("  /kmail send <address> [message]");
            sender.sendMessage("  /kmail read");
            sender.sendMessage("");
            sender.sendMessage("Do /kmail help <command> for help on any subcommand.");
            sender.sendMessage("Other help topics: addresses");
            return true;
        }
        
        String topic = args[0];
        
        if (topic.equalsIgnoreCase("send")) {
            sender.sendMessage("/kmail send <address> [message]");
            sender.sendMessage("Send a message to the specified address. If the message is not specified in the command, all future chat messages (until one consisting of a single period ('.') is sent) will be used as the body of the message.");
            sender.sendMessage("See also: /kmail help addresses");
            return true;
        }
        
        if (topic.equalsIgnoreCase("read")) {
            sender.sendMessage("/kmail read");
            sender.sendMessage("Displays the oldest unread message and marks it as read.");
            return true;
        }
        
        if (topic.equalsIgnoreCase("list")) {
            sender.sendMessage("/kmail list <tag>");
            sender.sendMessage("Lists messages with the given tag.");
            return true;
        }
        
        if (topic.equalsIgnoreCase("addresses")) {
            sender.sendMessage("An address can be:");
            sender.sendMessage("  kierdavis");
            sender.sendMessage("    - The username of a player on the local server");
            sender.sendMessage("  kierdavis@mc.example.net");
            sender.sendMessage("    - A user on another server (replace 'mc.example.net' with the same IP address/domain name used to connect to it from the Minecraft client)");
            sender.sendMessage("  *");
            sender.sendMessage("    - All players on the local server");
            return true;
        }
        
        sender.sendMessage("Invalid help topic: " + topic);
        return false;
    }
    
    private boolean doSend(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /kmail send <address> [message]");
            sender.sendMessage("See /kmail help send for more info.");
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
            
            sender.sendMessage("Mail queued.");
            
            return true;
        }
        
        else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command must be run as a player.");
                return false;
            }
            
            Player player = (Player) sender;
            PartialMessage pm = new PartialMessage(msg);
            
            plugin.putPartialMessage(player, pm);
            
            sender.sendMessage("Now type your mail message in normal chat (not with commands).");
            sender.sendMessage("End the message with a single chat message containing a dot (period).");
            
            return true;
        }
    }
    
    private boolean doRead(CommandSender sender, String[] args) {
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Iterator it = mb.iterator();
        
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            
            if (!msg.isRead()) {
                displayMessage(sender, msg);
                msg.setRead(true);
                return true;
            }
        }
        
        sender.sendMessage("No unread messages.");
        return true;
    }
    
    private boolean doList(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /kmail list <tag>");
            sender.sendMessage("See /kmail help list for more info.");
            return false;
        }
        
        String tag = args[0];
        Mailbox mb = plugin.getMailbox(getUsername(sender));
        Iterator it = mb.iterator();
        
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            
            if (msg.hasTag(tag)) {
                displayMessageSummary(sender, msg);
            }
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
        sender.sendMessage("================================");
        sender.sendMessage("From: " + msg.getSrcAddress().toString());
        sender.sendMessage("To: " + msg.getDestAddress().toString());
        sender.sendMessage("Sent: " + msg.getSentDate().toString());
        sender.sendMessage("Recieved: " + msg.getReceivedDate().toString());
        sender.sendMessage("");
        sender.sendMessage(msg.getBody());
        sender.sendMessage("================================");
    }
    
    private void displayMessageSummary(CommandSender sender, Message msg) {
        String bodySummary = msg.getBody();
        
        if (bodySummary.length > 18) {
            bodySummary = bodySummary.substring(15) + "...";
        }
        
        StringBuilder b = new StringBuilder();
        
        if (!msg.isRead()) {
            b.append("*");
        }
                
        b.append(msg.getLocalID());
        b.append(" ");
        b.append(msg.getSrcAddress().toString());
        b.append(" ");
        b.append(bodySummary);
        
        sender.sendMessage(b.toString());
    }
}
