package com.kierdavis.kmail;

public class Message {
    private final long localID;
    private Address src;
    private Address dest;
    private String body;
    
    // Only used in mailboxes
    private boolean read;
    
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
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean x) {
        read = x;
    }
}
