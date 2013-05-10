package com.kierdavis.kmail;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MailSendEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private Message msg;
    
    public MailSendEvent(Message msg_) {
        msg = msg_;
    }
    
    public Message getMessage() {
        return msg;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
