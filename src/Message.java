package com.kierdavis.kmail;

public class Message {
    private Address src;
    private Address dest;
    private String body;
    
    // Only used in mailboxes
    private boolean read;
    
    public Message(Address src, Address dest) {
        this.src = src;
        this.dest = dest;
        this.body = "";
        
        this.read = false;
    }
    
    public Message(Address src, Address dest, String body) {
        this.src = src;
        this.dest = dest;
        this.body = body;
        
        this.read = false;
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
