package com.kierdavis.kmail;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Message {
    private final long localID;
    private Address src;
    private Address dest;
    private String body;
    private Date sentDate;
    
    // Only used in mailboxes
    private Date receivedDate;
    private Set<String> tags;
    
    private static long nextLocalID = 0;
    
    private static synchronized long getNextLocalID() {
        nextLocalID++;
        return nextLocalID;
    }
    
    public Message(Address src, Address dest) {
        this.localID = Message.getNextLocalID();
        this.src = src;
        this.dest = dest;
        this.body = "";
        this.sentDate = null;
    }
    
    public void initReceived() {
        this.receivedDate = null;
        this.tags = new HashSet<String>();
        
        markUnread();
    }
    
    public final long getLocalID() {
        return localID;
    }
    
    public Address getSrcAddress() {
        return src;
    }
    
    public void setSrcAddress(Address x) {
        src = x;
    }
    
    public Address getDestAddress() {
        return dest;
    }
    
    public void setDestAddress(Address x) {
        dest = x;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String x) {
        body = x;
    }
    
    public Date getSentDate() {
        return sentDate;
    }
    
    public void setSentDate(Date x) {
        sentDate = x;
    }
    
    public Date getReceivedDate() {
        return receivedDate;
    }
    
    public void setReceivedDate(Date x) {
        receivedDate = x;
    }
    
    public void addTag(String tag) {
        tags.add(tag.toLowerCase());
    }
    
    public boolean hasTag(String tag) {
        return tags.contains(tag.toLowerCase());
    }
    
    public void removeTag(String tag) {
        tags.remove(tag.toLowerCase());
    }
    
    public boolean isUnread() {
        return hasTag("unread");
    }
    
    public boolean isRead() {
        return !isUnread();
    }
    
    public void markRead() {
        removeTag("unread");
    }
    
    public void markUnread() {
        addTag("unread");
    }
}
