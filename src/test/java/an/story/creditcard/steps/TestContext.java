package an.story.creditcard.steps;

import an.story.creditcard.controller.TransactionController;
import org.apache.avro.generic.GenericRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared test context for Cucumber step definitions.
 * Provides access to test infrastructure including the API controller
 * and captured Kafka messages.
 */
public class TestContext {

    private static TestContext instance;

    private TransactionController controller;
    private final Map<String, List<GenericRecord>> capturedMessagesByTopic;
    private final Map<String, Object> sharedData;

    private TestContext() {
        this.capturedMessagesByTopic = new ConcurrentHashMap<>();
        this.sharedData = new ConcurrentHashMap<>();
    }

    public static synchronized TestContext getInstance() {
        if (instance == null) {
            instance = new TestContext();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public TransactionController getController() {
        return controller;
    }

    public void setController(TransactionController controller) {
        this.controller = controller;
    }

    public List<GenericRecord> getCapturedMessages(String topic) {
        return capturedMessagesByTopic.getOrDefault(topic, Collections.emptyList());
    }

    public void addCapturedMessage(String topic, GenericRecord message) {
        capturedMessagesByTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(message);
    }

    public void clearCapturedMessages(String topic) {
        capturedMessagesByTopic.remove(topic);
    }

    public void clearAllCapturedMessages() {
        capturedMessagesByTopic.clear();
    }

    public void put(String key, Object value) {
        sharedData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) sharedData.get(key);
    }

    public void clear() {
        sharedData.clear();
        capturedMessagesByTopic.clear();
    }
}
