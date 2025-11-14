package an.story.gherkin_generator.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import an.story.gherkin_generator.MultiServiceTestHarness;

/**
 * Context shared across step definitions in Cucumber tests
 */
public class TestContext {
    private Map<String, Object> data = new ConcurrentHashMap<>();
    private volatile MultiServiceTestHarness harness;
    
    public void set(String key, Object value) {
        data.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) data.get(key);
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    public void setHarness(MultiServiceTestHarness harness) {
        this.harness = harness;
    }
    
    public MultiServiceTestHarness getHarness() {
        return harness;
    }
    
    public void clear() {
        data.clear();
    }
}
