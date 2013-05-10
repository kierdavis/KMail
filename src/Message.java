package com.kierdavis.kmail;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Message {
    private Address src;
    private Address dest;
    private String body;
    private Date sentDate;
    
    // Only used internally
    private Date receivedDate;
    private long localID;
    private Set<String> tags;
    private int numRetries;
    
    private static long nextLocalID = 0;
    private static synchronized long getNextLocalID() {
        nextLocalID++;
        return nextLocalID;
    }
    
    public Message() {
        src = null;
        dest = null;
        body = "";
        sentDate = null;
        receivedDate = null;
        localID = 0;
        tags = new HashSet<String>();
        numRetries = 0;
        
        assignLocalID();
    }
    
    public Message(Address src, Address dest) {
        this();
        setSrcAddress(src);
        setDestAddress(dest);
    }
    
    public final long getLocalID() {
        return localID;
    }
    
    public void assignLocalID() {
        localID = Message.getNextLocalID();
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
    
    public Set<String> getTags() {
        return tags;
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
    
    public void incRetries() {
        numRetries++;
    }
    
    public int getRetries() {
        return numRetries;
    }
    
    public Message clone() {
        Message msg = new Message();
        msg.src = src.clone();
        msg.dest = dest.clone();
        msg.body = body;
        msg.sentDate = sentDate;
        msg.receivedDate = receivedDate;
        msg.tags = new HashSet<String>(tags);
        return msg;
    }
}
