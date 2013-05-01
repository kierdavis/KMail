package com.kierdavis.kmail;

public interface SearchCriteria {
    public boolean match(Message msg);
    
    public static SearchCritera parse(String s) {
        if (s.length < 3 || s.charAt(1) != ':') {
            return null;
        }
        
        char c = s.charAt(0);
        String arg = s.substring(2);
        
        switch (c) {
        case 't':
            return TagSearchCriteria(arg);
        default:
            return null;
        }
    }
}
