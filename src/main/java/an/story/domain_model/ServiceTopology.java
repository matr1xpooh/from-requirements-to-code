package an.story.domain_model;

import java.util.List;
/**
 * Represents the service topology extracted from a story
 */
public class ServiceTopology {
    private List<String> services;
    private List<String> events;
    private List<String> schemas;

    public ServiceTopology(List<String> services, List<String> events, List<String> schemas) {
        this.services = services;
        this.events = events;
        this.schemas = schemas;
    }

    public List<String> getServices() { return services; }
    public List<String> getEvents() { return events; }
    public List<String> getSchemas() { return schemas; }

    @Override
    public String toString() {
        return "ServiceTopology{" +
                "services=" + services +
                ", events=" + events +
                ", schemas=" + schemas +
                '}';
    }
}