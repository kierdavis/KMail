package com.kierdavis.kmail;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Mailbox {
    private List<Message> messages;
    
    public Mailbox() {
        messages = new ArrayList<Message>();
    }
    
    public void add(Message msg) {
        msg.setRead(false);
        msg.setReceivedDate(new Date());
        
        messages.add(msg);
    }
    
    public Iterator<Message> iterator() {
        return messages.iterator();
    }
}
