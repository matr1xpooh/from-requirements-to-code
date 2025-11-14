package an.story.gherkin_generator;

import java.util.*;

import an.story.gherkin_generator.model.Event;

/**
 * Test harness for multi-service acceptance testing
 * This is a reusable framework class
 */
public class MultiServiceTestHarness {
    private Map<String, ServiceClient> serviceClients = new HashMap<>();
    private EventBusSpy eventBusSpy = new EventBusSpy();
    private SchemaRegistryClient schemaRegistry = new SchemaRegistryClient();
    private List<String> configuredServices;
    private List<String> configuredEvents;
    
    public MultiServiceTestHarness(List<String> services, List<String> events) {
        this.configuredServices = services != null ? services : new ArrayList<>();
        this.configuredEvents = events != null ? events : new ArrayList<>();
    }
    
    public void setup() throws Exception {
        // Initialize service clients
        for (String service : configuredServices) {
            serviceClients.put(service, new ServiceClient(service));
        }
        
        // Subscribe to events
        for (String event : configuredEvents) {
            eventBusSpy.subscribe(event);
        }
    }
    
    public void teardown() throws Exception {
        serviceClients.values().forEach(ServiceClient::close);
        eventBusSpy.close();
    }
    
    public void reset() {
        eventBusSpy.clear();
    }
    
    public ServiceClient getService(String serviceName) {
        ServiceClient client = serviceClients.get(serviceName);
        if (client == null) {
            throw new IllegalArgumentException("Service not configured: " + serviceName);
        }
        return client;
    }
    
    public Event waitForEvent(String eventType, int timeoutSeconds) throws Exception {
        Event event = eventBusSpy.waitForEvent(eventType, timeoutSeconds);
        if (event == null) {
            throw new AssertionError("Event " + eventType + " was not received within " + timeoutSeconds + " seconds");
        }
        return event;
    }
    
    public void verifyEventNotPublished(String eventType) {
        if (eventBusSpy.hasEvent(eventType)) {
            throw new AssertionError("Event " + eventType + " should not have been published");
        }
    }
    
    public void verifySchemaConformance(Event event, String schemaName) throws Exception {
        schemaRegistry.validate(event, schemaName);
    }
    
    public List<Event> getEvents(String eventType) {
        return eventBusSpy.getEvents(eventType);
    }
    
    public EventBusSpy getEventBusSpy() {
        return eventBusSpy;
    }
}

