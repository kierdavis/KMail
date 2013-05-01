package com.kierdavis.kmail;

public interface SearchCriteria {
    public boolean match(Message msg);
}
