Run ScienceTeacherAgent: 

follow `https://google.github.io/adk-docs/get-started/streaming/quickstart-streaming-java/#run-dev-ui`

```
export ANTHROPIC_API_KEY="$(cat ./anthropic-api-key.txt)"

mvn exec:java \
    -Dexec.mainClass="com.google.adk.web.AdkWebServer" \
    -Dexec.args="--adk.agents.source-dir=src/main/java" \
    -Dexec.classpathScope="compile"
 ```
open UI at `http://127.0.0.1:8080/`, select `science-app` from the dropdown, ask `what's the most difficult problem in arithmetic?`

##testing
```
mvn clean install
mvn exec:java -Dexec.args="--server.port=8083 --adk.agents.source-dir=src/main/java/agents/multitool"
```