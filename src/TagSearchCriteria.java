package com.kierdavis.kmail;

public class TagSearchCriteria implements SearchCriteria {
    private String tag;
    
    public TagSearchCriteria(String tag) {
        this.tag = tag;
    }
    
    public String getTag() {
        return tag;
    }
    
    public boolean match(Message msg) {
        return msg.hasTag(tag);
    }
}
