package an.story.gherkin_generator.generic;

/**
 * Represents an API endpoint that can be tested.
 * Used by domain mappings to define which endpoints handle specific operations.
 */
public class ApiEndpoint {

    private final String method;
    private final String path;
    private final String description;
    private final String requestContentType;

    public ApiEndpoint(String method, String path, String description) {
        this(method, path, description, "application/json");
    }

    public ApiEndpoint(String method, String path, String description, String requestContentType) {
        this.method = method.toUpperCase();
        this.path = path;
        this.description = description;
        this.requestContentType = requestContentType;
    }

    public static ApiEndpoint post(String path, String description) {
        return new ApiEndpoint("POST", path, description);
    }

    public static ApiEndpoint get(String path, String description) {
        return new ApiEndpoint("GET", path, description);
    }

    public static ApiEndpoint put(String path, String description) {
        return new ApiEndpoint("PUT", path, description);
    }

    public static ApiEndpoint delete(String path, String description) {
        return new ApiEndpoint("DELETE", path, description);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getRequestContentType() {
        return requestContentType;
    }

    @Override
    public String toString() {
        return method + " " + path;
    }
}
