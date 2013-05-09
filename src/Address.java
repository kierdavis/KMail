package com.kierdavis.kmail;

import java.io.Serializable;

public class Address implements Serializable {
    private String username;
    private String hostname;
    
    public Address(String s) {
        int pos = s.indexOf('@');
        
        if (pos < 0) {
            username = s;
            hostname = "local";
        }
        
        else {
            username = s.substring(0, pos);
            hostname = s.substring(pos + 1);
        }
    }
    
    public Address(String username_, String hostname_) {
        username = username_;
        hostname = hostname_;
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
