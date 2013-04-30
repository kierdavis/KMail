package com.kierdavis.kmail;

public class Message {
    private Address src;
    private Address dest;
    private String body;
    
    public Message(Address src, Address dest) {
        this.src = src;
        this.dest = dest;
        this.body = "";
    }
    
    public Message(Address src, Address dest, String body) {
        this.src = src;
        this.dest = dest;
        this.body = body;
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
}
