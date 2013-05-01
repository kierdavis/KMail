package com.kierdavis.kmail;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Mailbox {
    private List<Message> messages;
    
    public Mailbox() {
        messages = new ArrayList<Message>();
    }
    
    public void add(Message msg) {
        msg.initReceived();
        msg.setReceivedDate(new Date());
        msg.markUnread();
        
        messages.add(msg);
    }
    
    public Iterator<Message> iterator() {
        return messages.iterator();
    }
    
    public Iterator<Message> search(Set<SearchCriteria> criteria) {
        if (criteria.size() == 0) {
            return messages.iterator();
        }
        
        return (Iterator<Message>) (new SearchIterator(messages.iterator(), criteria));
    }
}
