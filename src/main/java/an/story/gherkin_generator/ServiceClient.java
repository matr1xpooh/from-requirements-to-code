package an.story.gherkin_generator;

/**
 * Client for calling a service
 */
public class ServiceClient {
    private String serviceName;
    private String baseUrl;
    
    public ServiceClient(String serviceName) {
        this.serviceName = serviceName;
        // In real implementation, would get URL from config/discovery
        this.baseUrl = "http://localhost:8080/" + serviceName.replace(" ", "-");
    }
    
    public ServiceClient(String serviceName, String baseUrl) {
        this.serviceName = serviceName;
        this.baseUrl = baseUrl;
    }
    
    public <T> T post(String endpoint, Object request, Class<T> responseType) {
        // TODO: Implement actual HTTP call
        throw new UnsupportedOperationException("Implement HTTP POST to " + baseUrl + endpoint);
    }
    
    public <T> T get(String endpoint, Class<T> responseType) {
        // TODO: Implement actual HTTP call
        throw new UnsupportedOperationException("Implement HTTP GET to " + baseUrl + endpoint);
    }
    
    public String getServiceName() { return serviceName; }
    
    public void close() {
        // Cleanup resources
    }
}

