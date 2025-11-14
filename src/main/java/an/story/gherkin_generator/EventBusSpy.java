package an.story.gherkin_generator;

import java.util.*;
import java.util.concurrent.*;

import an.story.gherkin_generator.model.Event;

/**
 * Spy for capturing events from the event bus
 */
public class EventBusSpy {
    private Map<String, BlockingQueue<Event>> eventQueues = new ConcurrentHashMap<>();
    private Map<String, List<Event>> capturedEvents = new ConcurrentHashMap<>();
    
    public void subscribe(String eventType) {
        eventQueues.putIfAbsent(eventType, new LinkedBlockingQueue<>());
        capturedEvents.putIfAbsent(eventType, new CopyOnWriteArrayList<>());
    }
    
    public void recordEvent(Event event) {
        String eventType = event.getEventType();
        BlockingQueue<Event> queue = eventQueues.get(eventType);
        if (queue != null) {
            queue.offer(event);
            capturedEvents.get(eventType).add(event);
        }
    }
    
    public Event waitForEvent(String eventType, int timeoutSeconds) throws InterruptedException {
        BlockingQueue<Event> queue = eventQueues.get(eventType);
        if (queue == null) {
            throw new IllegalStateException("Not subscribed to event type: " + eventType);
        }
        return queue.poll(timeoutSeconds, TimeUnit.SECONDS);
    }
    
    public boolean hasEvent(String eventType) {
        List<Event> events = capturedEvents.get(eventType);
        return events != null && !events.isEmpty();
    }
    
    public List<Event> getEvents(String eventType) {
        return new ArrayList<>(capturedEvents.getOrDefault(eventType, Collections.emptyList()));
    }
    
    public void clear() {
        eventQueues.values().forEach(BlockingQueue::clear);
        capturedEvents.values().forEach(List::clear);
    }
    
    public void close() {
        clear();
    }
}

