package com.kierdavis.kmail;

import java.util.Arrays;
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
        
        sender.sendMessage("Invalid subcommand: " + subcmd);
        return false;
    }
    
    private boolean doHelp(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("KMail Help: (<required> [optional])");
            sender.sendMessage("  /kmail send <address> [message]");
            sender.sendMessage("");
            sender.sendMessage("Do /kmail help <command> for help on any subcommand.");
            sender.sendMessage("Other help topics: addresses");
            return true;
        }
        
        String topic = args[0];
        
        if (topic.equalsIgnoreCase("send")) {
            sender.sendMessage("/kmail send <address> [message]");
            sender.sendMessage("Send a message to the specified address. If the message is not specified in the command, all future chat messages (until one consisting of a single hyphen ('-') is sent) will be used as the body of the message.");
            sender.sendMessage("See also: /kmail help addresses");
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
        String srcHostname = plugin.getLocalHostname();
        
        if (sender instanceof Player) {
            srcUsername = ((Player) sender).getName();
        }
        else {
            srcUsername = "CONSOLE";
        }
        
        Address srcAddress = new Address(srcUsername, srcHostname);
        Address destAddress = new Address(args[0]);
        Message msg = new Message(srcAddress, destAddress);
        
        if (args.length >= 2) {
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append(args[1]);
            
            for (int i = 1; i < args.length; i++) {
                bodyBuilder.append(" ").append(args[i]);
            }
            
            msg.setBody(bodyBuilder.toString());
            
            sender.sendMessage("Mail:");
            sender.sendMessage("  From: " + msg.getSrcAddress().toString());
            sender.sendMessage("  To: " + msg.getDestAddress().toString());
            sender.sendMessage("  Body: " + msg.getBody());
            
            return true;
        }
        
        else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command must be run as a player.");
                return false;
            }
            
            Player player = (Player) sender;
            PartialMessage pm = new PartialMessage(msg);
            
            plugin.partialMessages.put(player, pm);
            return true;
        }
    }
}
