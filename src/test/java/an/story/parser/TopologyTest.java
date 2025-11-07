package an.story.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import an.story.domain_model.JiraStory;
import an.story.domain_model.ServiceTopology;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Service Topology Extraction")
class TopologyTests extends JiraStoryParserTest{
    
    @Test
    @DisplayName("Should extract service topology from story")
    void shouldExtractServiceTopology() {
        String storyText = "{panel:title=Value Statement}\n" +
            "As a user, I want something, so that benefit.\n" +
            "{panel}\n" +
            "{panel:title=Requirements}\n" +
            "1. update the \"data cleanse\" service\n" +
            "2. call the \"payment\" service\n" +
            "3. trigger the \"orderCreated\" event\n" +
            "4. update avro schema for the \"orderCompleted\" event\n" +
            "{panel}";
        
        JiraStory story = parser.parse(storyText);
        ServiceTopology topology = parser.extractTopology(story);
        
        assertEquals(2, topology.getServices().size());
        assertTrue(topology.getServices().contains("data cleanse"));
        assertTrue(topology.getServices().contains("payment"));
        
        assertEquals(2, topology.getEvents().size());
        assertTrue(topology.getEvents().contains("orderCreated"));
        assertTrue(topology.getEvents().contains("orderCompleted"));
        
        assertEquals(1, topology.getSchemas().size());
        assertTrue(topology.getSchemas().contains("orderCompleted.avsc"));
    }
    
    @Test
    @DisplayName("Should deduplicate services and events in topology")
    void shouldDeduplicateInTopology() {
        String storyText = "{panel:title=Value Statement}\n" +
            "As a user, I want something, so that benefit.\n" +
            "{panel}\n" +
            "{panel:title=Requirements}\n" +
            "1. update the \"data cleanse\" service\n" +
            "2. call the \"data cleanse\" service again\n" +
            "3. trigger the \"orderCreated\" event\n" +
            "4. publish the \"orderCreated\" event\n" +
            "{panel}";
        
        JiraStory story = parser.parse(storyText);
        ServiceTopology topology = parser.extractTopology(story);
        
        assertEquals(1, topology.getServices().size());
        assertEquals(1, topology.getEvents().size());
    }
}