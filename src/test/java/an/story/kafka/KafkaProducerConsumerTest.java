package an.story.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
// import org.springframework.kafka.test.context.EmbeddedKafka;
// import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Kafka producer and consumer using an in-memory Kafka broker.
 * Uses Spring Kafka Test's embedded Kafka (no Docker required).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaProducerConsumerTest {

    private static final String TEST_TOPIC = "test-story-events";
    private static final String TEST_GROUP_ID = "test-consumer-group";
    
    private EmbeddedKafkaBroker embeddedKafka;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    @BeforeAll
    void setup() throws Exception {
        // Initialize embedded Kafka with 1 broker, controlled shutdown, 1 partition per topic
        //embeddedKafka = new EmbeddedKafkaBroker(1, true, 1, TEST_TOPIC)
        //    .brokerProperty("auto.create.topics.enable", "true");
        embeddedKafka = new org.springframework.kafka.test.EmbeddedKafkaZKBroker(1, true, 1, TEST_TOPIC);
        embeddedKafka.afterPropertiesSet();

        // Create producer
        producer = new KafkaProducer<>(getProducerProps());
        
        // Create consumer
        consumer = new KafkaConsumer<>(getConsumerProps());
        consumer.subscribe(Collections.singletonList(TEST_TOPIC));
    }

    @AfterAll
    void teardown() {
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
        if (embeddedKafka != null) {
            embeddedKafka.destroy();
        }
    }

    @Test
    void testProducerSendsMessageSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        String key = "story-123";
        String value = "Story parsed successfully";

        // When
        Future<RecordMetadata> future = producer.send(
            new ProducerRecord<>(TEST_TOPIC, key, value)
        );
        RecordMetadata metadata = future.get();

        // Then
        assertNotNull(metadata);
        assertEquals(TEST_TOPIC, metadata.topic());
        assertTrue(metadata.offset() >= 0);
        System.out.println("Message sent to partition " + metadata.partition() 
                         + " with offset " + metadata.offset());
    }

    @Test
    void testConsumerReceivesMessage() throws Exception {
        // Given - produce a message first
        String key = "story-456";
        String value = "New requirement detected";
        producer.send(new ProducerRecord<>(TEST_TOPIC, key, value)).get();
        producer.flush();

        // When - consume the message
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

        // Then
        assertFalse(records.isEmpty(), "Consumer should have received at least one message");
        
        boolean foundMessage = false;
        for (ConsumerRecord<String, String> record : records) {
            if (key.equals(record.key()) && value.equals(record.value())) {
                foundMessage = true;
                assertEquals(TEST_TOPIC, record.topic());
                assertTrue(record.offset() >= 0);
                System.out.println("Consumed message - Key: " + record.key() 
                                 + ", Value: " + record.value() 
                                 + ", Offset: " + record.offset());
            }
        }
        
        assertTrue(foundMessage, "Should have found the expected message");
    }

    @Test
    void testMultipleMessagesProducedAndConsumed() throws Exception {
        // Given - produce multiple messages
        List<String> sentMessages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String key = "story-" + i;
            String value = "Story content " + i;
            sentMessages.add(key + ":" + value);
            producer.send(new ProducerRecord<>(TEST_TOPIC, key, value)).get();
        }
        producer.flush();

        // When - consume messages
        Set<String> receivedMessages = new HashSet<>();
        long endTime = System.currentTimeMillis() + 15000; // 15 second timeout
        
        while (System.currentTimeMillis() < endTime && receivedMessages.size() < sentMessages.size()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
            for (ConsumerRecord<String, String> record : records) {
                receivedMessages.add(record.key() + ":" + record.value());
            }
        }

        // Then
        assertEquals(sentMessages.size(), receivedMessages.size(), 
                    "Should receive all sent messages");
        assertTrue(receivedMessages.containsAll(sentMessages), 
                  "All sent messages should be received");
    }

    @Test
    void testProducerWithNullKey() throws Exception {
        // Given
        String value = "Message with no key";

        // When
        RecordMetadata metadata = producer.send(
            new ProducerRecord<>(TEST_TOPIC, null, value)
        ).get();

        // Then
        assertNotNull(metadata);
        assertEquals(TEST_TOPIC, metadata.topic());
        
        // Verify consumer can read it
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        boolean found = false;
        for (ConsumerRecord<String, String> record : records) {
            if (value.equals(record.value())) {
                assertNull(record.key(), "Key should be null");
                found = true;
            }
        }
        assertTrue(found, "Should find message with null key");
    }

    @Test
    void testConsumerCommitsOffset() throws Exception {
        // Given - send a message
        String key = "offset-test";
        String value = "Testing offset commit";
        producer.send(new ProducerRecord<>(TEST_TOPIC, key, value)).get();
        producer.flush();

        // When - consume and commit
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());
        
        ConsumerRecord<String, String> record = records.iterator().next();
        consumer.commitSync(); // Manually commit offsets

        // Then - verify offset was committed
        var topicPartition = new org.apache.kafka.common.TopicPartition(
            record.topic(), 
            record.partition()
        );
        var committed = consumer.committed(Set.of(topicPartition), Duration.ofSeconds(5));
        
        assertNotNull(committed, "Should have committed offset");
        assertTrue(committed.containsKey(topicPartition), "Should contain the topic partition");
        assertNotNull(committed.get(topicPartition), "Offset should be committed");
    }

    private Properties getProducerProps() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return props;
    }

    private Properties getConsumerProps() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, TEST_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        return props;
    }
}