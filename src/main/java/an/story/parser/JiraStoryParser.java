package an.story.parser;

import java.util.*;
import java.util.regex.*;

import an.story.domain_model.JiraStory;
import an.story.domain_model.ValueStatement;
import an.story.domain_model.Requirement;
import an.story.domain_model.AcceptanceCriterion;
import an.story.domain_model.Panel;
import an.story.domain_model.ServiceTopology;


// Parser Implementation

/**
 * Main parser for Jira stories in panel format
 */
public class JiraStoryParser {
    
    private static final Pattern PANEL_PATTERN = Pattern.compile(
        "\\{panel:title=([^|}]+)(?:[^}]+)?\\}\\s*(.*?)\\s*\\{panel\\}",
        Pattern.DOTALL
    );

    /**
     * Parse a complete Jira story
     */
    public JiraStory parse(String storyText) {
        if (storyText == null || storyText.trim().isEmpty()) {
            throw new IllegalArgumentException("Story text cannot be null or empty");
        }

        List<Panel> panels = extractPanels(storyText);
        
        ValueStatement valueStatement = null;
        List<Requirement> requirements = new ArrayList<>();
        List<AcceptanceCriterion> acceptanceCriteria = new ArrayList<>();

        for (Panel panel : panels) {
            String title = panel.getTitle().toLowerCase();
            
            if (title.contains("value statement")) {
                valueStatement = parseValueStatement(panel.getContent());
            } else if (title.contains("requirements")) {
                requirements = parseRequirements(panel.getContent());
            } else if (title.contains("acceptance criteria")) {
                acceptanceCriteria = parseAcceptanceCriteria(panel.getContent());
            }
        }

        if (valueStatement == null) {
            throw new IllegalArgumentException("Story must contain a Value Statement");
        }

        return new JiraStory(valueStatement, requirements, acceptanceCriteria);
    }

    /**
     * Extract all panels from the story text
     */
    private List<Panel> extractPanels(String storyText) {
        List<Panel> panels = new ArrayList<>();
        Matcher matcher = PANEL_PATTERN.matcher(storyText);
        
        while (matcher.find()) {
            String title = matcher.group(1).trim();
            String content = matcher.group(2).trim();
            panels.add(new Panel(title, content));
        }
        
        return panels;
    }

    /**
     * Parse the value statement (As a... I want... so that...)
     */
    private ValueStatement parseValueStatement(String content) {
        Pattern pattern = Pattern.compile(
            "As a\\s+([^,]+),\\s*I want(?:\\s+to)?\\s+(.+?),\\s*so that\\s+(.+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String persona = matcher.group(1).trim();
            String goal = matcher.group(2).trim();
            String benefit = matcher.group(3).trim();
            
            return new ValueStatement(persona, goal, benefit);
        }
        
        throw new IllegalArgumentException("Invalid value statement format: " + content);
    }

    /**
     * Parse numbered requirements list
     */
    private List<Requirement> parseRequirements(String content) {
        List<Requirement> requirements = new ArrayList<>();
        
        // Split by line and find numbered items
        String[] lines = content.split("\\r?\\n");
        StringBuilder currentRequirement = new StringBuilder();
        Integer currentNumber = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Check if this line starts with a number
            Pattern numberPattern = Pattern.compile("^(\\d+)\\.\\s*(.*)");
            Matcher matcher = numberPattern.matcher(line);
            
            if (matcher.find()) {
                // Save previous requirement if exists
                if (currentNumber != null && currentRequirement.length() > 0) {
                    requirements.add(new Requirement(currentNumber, 
                                                    currentRequirement.toString().trim()));
                }
                
                // Start new requirement
                currentNumber = Integer.parseInt(matcher.group(1));
                currentRequirement = new StringBuilder(matcher.group(2));
            } else if (currentNumber != null) {
                // Continuation of current requirement
                currentRequirement.append(" ").append(line);
            }
        }
        
        // Add last requirement
        if (currentNumber != null && currentRequirement.length() > 0) {
            requirements.add(new Requirement(currentNumber, 
                                            currentRequirement.toString().trim()));
        }
        
        return requirements;
    }

    /**
     * Parse Gherkin-style acceptance criteria
     */
    private List<AcceptanceCriterion> parseAcceptanceCriteria(String content) {
        List<AcceptanceCriterion> criteria = new ArrayList<>();
        
        String[] lines = content.split("\\r?\\n");
        AcceptanceCriterion current = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.startsWith("Scenario:")) {
                if (current != null) {
                    criteria.add(current);
                }
                String scenarioName = line.substring("Scenario:".length()).trim();
                current = new AcceptanceCriterion(scenarioName);
            } else if (current != null) {
                if (line.startsWith("Given ")) {
                    current.addGiven(line.substring("Given ".length()).trim());
                } else if (line.startsWith("When ")) {
                    current.addWhen(line.substring("When ".length()).trim());
                } else if (line.startsWith("Then ")) {
                    current.addThen(line.substring("Then ".length()).trim());
                } else if (line.startsWith("And ")) {
                    // Add to the most recent statement type
                    String statement = line.substring("And ".length()).trim();
                    if (!current.getThenStatements().isEmpty()) {
                        current.addThen(statement);
                    } else if (!current.getWhenStatements().isEmpty()) {
                        current.addWhen(statement);
                    } else if (!current.getGivenStatements().isEmpty()) {
                        current.addGiven(statement);
                    }
                }
            }
        }
        
        if (current != null) {
            criteria.add(current);
        }
        
        return criteria;
    }

    /**
     * Extract service topology from parsed story
     */
    public ServiceTopology extractTopology(JiraStory story) {
        Set<String> services = new HashSet<>();
        Set<String> events = new HashSet<>();
        Set<String> schemas = new HashSet<>();
        
        for (Requirement req : story.getRequirements()) {
            services.addAll(req.getServices());
            events.addAll(req.getEvents());
            schemas.addAll(req.getSchemas());
        }
        
        return new ServiceTopology(
            new ArrayList<>(services),
            new ArrayList<>(events),
            new ArrayList<>(schemas)
        );
    }
}
