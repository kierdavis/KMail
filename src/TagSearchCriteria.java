package com.kierdavis.kmail;

public class TagSearchCriteria implements SearchCriteria {
    private String tag;
    
    public TagSearchCriteria(String tag_) {
        tag = tag_;
    }
    
    public String getTag() {
        return tag;
    }
    
    public boolean match(Message msg) {
        return msg.hasTag(tag);
    }
}
