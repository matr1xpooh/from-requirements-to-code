package an.story.domain_model;

/**
 * Represents a single panel from the Jira story
 */
public class Panel {
    private String title;
    private String content;

    public Panel(String title, String content) {
        this.title = title;
        this.content = content.trim();
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
}