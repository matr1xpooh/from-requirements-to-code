package an.story.domain_model;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Represents a single requirement with extracted service/event information
 */
public class Requirement {
    private static final Pattern SERVICE_PATTERN = Pattern.compile(
        "\"([^\"]+)\"\\s+service",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EVENT_PATTERN = Pattern.compile("\"(\\w+)\"\\s+event");

    private static final Pattern SCHEMA_PATTERN = Pattern.compile("\"(\\w+)\"\\s+event");

    private int number;
    private String text;
    private List<String> services;
    private List<String> events;
    private List<String> schemas;

    public Requirement(int number, String text) {
        this.number = number;
        this.text = text;
        this.services = extractServices(text);
        this.events = extractEvents(text);
        this.schemas = extractSchemas(text);
    }

    private List<String> extractServices(String text) {
        List<String> services = new ArrayList<>();
        Matcher matcher = SERVICE_PATTERN.matcher(text);
        while (matcher.find()) {
            services.add(matcher.group(1));
        }
        return services;
    }

    private List<String> extractEvents(String text) {
        List<String> events = new ArrayList<>();
        Matcher matcher = EVENT_PATTERN.matcher(text);
        while (matcher.find()) {
            events.add(matcher.group(1));
        }
        return events;
    }

    private List<String> extractSchemas(String text) {
        List<String> schemas = new ArrayList<>();
        if (text.toLowerCase().contains("avro schema")) {
            Matcher matcher = SCHEMA_PATTERN.matcher(text);
            while (matcher.find()) {
                schemas.add(matcher.group(1) + ".avsc");
            }
        }
        return schemas;
    }

    public int getNumber() { return number; }
    public String getText() { return text; }
    public List<String> getServices() { return services; }
    public List<String> getEvents() { return events; }
    public List<String> getSchemas() { return schemas; }

    @Override
    public String toString() {
        return String.format("%d. %s [services=%s, events=%s]", 
                           number, text, services, events);
    }
}