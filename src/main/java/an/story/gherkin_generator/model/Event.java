package an.story.gherkin_generator.model;

import java.util.*;

/**
 * Represents an event in the system
 */
public class Event {
    private String eventType;
    private Map<String, Object> data;
    private long timestamp;
    
    public Event(String eventType, Map<String, Object> data) {
        this.eventType = eventType;
        this.data = data != null ? data : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getEventType() { return eventType; }
    public Map<String, Object> getData() { return data; }
    public long getTimestamp() { return timestamp; }
    
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> type) {
        return (T) data.get(key);
    }
}
