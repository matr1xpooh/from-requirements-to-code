package an.story.gherkin_generator.command;

import an.story.domain_model.ServiceTopology;

/**
 * Command to generate a Background section from a ServiceTopology
 */
public class GenerateBackgroundCommand implements Command<String> {
    private final ServiceTopology topology;
    
    public GenerateBackgroundCommand(ServiceTopology topology) {
        this.topology = topology;
    }
    
    @Override
    public String execute() {
        StringBuilder bg = new StringBuilder();
        bg.append("  Background:\n");
        
        for (String service : topology.getServices()) {
            bg.append("    Given the \"").append(service).append("\" service is running\n");
        }
        
        if (!topology.getEvents().isEmpty()) {
            bg.append("    And event consumers are ready to receive events\n");
        }
        
        if (!topology.getSchemas().isEmpty()) {
            bg.append("    And the Avro schema registry is accessible\n");
        }
        
        bg.append("\n");
        return bg.toString();
    }
}

