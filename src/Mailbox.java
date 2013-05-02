package com.kierdavis.kmail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Mailbox {
    private List<Message> messages;
    
    public Mailbox() {
        messages = new ArrayList<Message>();
    }
    
    public void clear() {
        messages.clear();
    }
    
    public void add(Message msg) {
        msg.initReceived();
        msg.setReceivedDate(new Date());
        
        messages.add(msg);
    }
    
    public Iterator<Message> iterator() {
        return messages.iterator();
    }
    
    public Iterator<Message> search(Set<SearchCriteria> criteria) {
        if (criteria.size() == 0) {
            return messages.iterator();
        }
        
        return (Iterator<Message>) (new SearchIterator(messages.iterator(), criteria));
    }
    
    public static Mailbox load(KMail plugin, String player) {
        File dir = new File(plugin.getDataFolder(), "mailboxes");
        File file = new File(dir, player.toLowerCase() + ".yml");
        
        return Mailbox.load(file);
    }
    
    public static Mailbox load(File file) {
        if (!file.exists()) {
            return null;
        }
        
        Mailbox mb = new Mailbox();
        
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<Map<String, String>> list = cfg.getMapList("mail");
        
        if (list == null) {
            return null;
        }
        
        Iterator<Map<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            Map<String, Object> m = (Map<String, Object>) it.next();
            Message msg = new Message();
            
            if (m.containsKey("src"))
                msg.setSrcAddress(new Address((String) m.get("src")));
            if (m.containsKey("dest"))
                msg.setDestAddress(new Address((String) m.get("dest")));
            if (m.containsKey("body"))
                msg.setBody((String) m.get("body"));
            if (m.containsKey("sent"))
                msg.setSentDate(new Date((long) m.get("sent")));
            if (m.containsKey("received"))
                msg.setReceivedDate(new Date((long) m.get("received")));
            
            if (m.containsKey("tags")) {
                List<String> tags = (List<String>) m.get("tags");
                Iterator<String> tagsIt = tags.iterator();
                
                while (tagsIt.hasNext()) {
                    msg.addTag((String) tagsIt.next());
                }
            }
            
            mb.add(msg);
        }
        
        return mb;
    }
    
    public void save(KMail plugin, String player) {
        File dir = new File(plugin.getDataFolder(), "mailboxes");
        File file = new File(dir, player.toLowerCase() + ".yml");
        
        save(file);
    }
    
    public void save(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }
}
