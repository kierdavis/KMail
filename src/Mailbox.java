package com.kierdavis.kmail;

import java.util.ArrayList;
import java.util.List;

public class Mailbox {
    private List<Message> messages;
    
    public Mailbox() {
        messages = new ArrayList<Message>();
    }
    
    public void add(Message msg) {
        messages.add(msg);
    }
}
