package an.story.gherkin_generator.command;

/**
 * Command interface for encapsulating operations as objects
 */
public interface Command<T> {
    /**
     * Executes the command and returns the result
     * @return the result of executing the command
     */
    T execute();
}

