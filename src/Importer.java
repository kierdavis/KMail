package com.kierdavis.kmail;

import org.bukkit.command.CommandSender;

public interface Importer {
    public void import(KMail plugin, CommandSender sender, String[] args);
}
