package com.kierdavis.kmail;

import java.lang.UnsupportedOperationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class SearchIterator implements Iterator<Message> {
    private Iterator<Message> it;
    private Set<SearchCriteria> criteria;
    private Message nextItem;
    
    public SearchIterator(Iterator<Message> it_, Set<SearchCriteria> criteria_) {
        it = it_;
        criteria = criteria_;
        nextItem = null;
    }
    
    public boolean hasNext() {
        findNextItem();
        return nextItem != null;
    }
    
    public Message next() throws NoSuchElementException {
        findNextItem();
        
        if (nextItem == null) {
            throw new NoSuchElementException();
        }
        
        Message msg = nextItem;
        nextItem = null;
        
        return msg;
    }
    
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    private void findNextItem() {
        if (nextItem != null) {
            return;
        }
        
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            
            if (match(msg)) {
                nextItem = msg;
                return;
            }
        }
    }
    
    private boolean match(Message msg) {
        Iterator<SearchCriteria> critIt = criteria.iterator();
        
        while (critIt.hasNext()) {
            SearchCriteria crit = (SearchCriteria) critIt.next();
            
            if (!crit.match(msg)) {
                return false;
            }
        }
        
        return true;
    }
}
