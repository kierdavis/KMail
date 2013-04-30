package com.kierdavis.kmail;

public class Address {
    private String username;
    private String hostname;
    
    public Address(String s) {
        int pos = s.indexOf('@');
        
        if (pos < 0) {
            this.username = s;
            this.hostname = "local";
        }
        
        else {
            this.username = s.substring(0, pos)
            this.hostname = s.substring(pos + 1);
        }
    }
    
    public Address(String username, String hostname) {
        this.username = username;
        this.hostname = hostname;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String s) {
        username = s;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String s) {
        hostname = s;
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(username);
        b.append("@");
        b.append(hostname);
        return b.toString();
    }
}
