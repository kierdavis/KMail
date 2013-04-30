package com.kierdavis.kmail;

import java.util.Date;

public class Message {
    private final long localID;
    private Address src;
    private Address dest;
    private String body;
    private Date sentDate;
    
    // Only used in mailboxes
    private boolean read;
    private Date receivedDate;
    
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
        
        this.read = false;
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
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean x) {
        read = x;
    }
    
    public Date getReceivedDate() {
        return receivedDate;
    }
    
    public void setReceivedDate(Date x) {
        receivedDate = x;
    }
}
