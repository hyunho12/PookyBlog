package pookyBlog.web;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.springframework.context.annotation.Import;
import pookyBlog.common.jwt.JwtCookieFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DockerConfigurationTest {

    @Test
    void webImportsOnlyTheRequiredCommonJwtCookieFactory() {
        Import imported = WebApplication.class.getAnnotation(Import.class);

        assertThat(imported).isNotNull();
        assertThat(imported.value()).containsExactly(JwtCookieFactory.class);
    }

    @Test
    void composeYamlContainsOnlyWebPublishedPort() throws IOException {
        Path compose = Path.of("..").resolve("compose.yaml").normalize();
        Map<String, Object> document = new Yaml().load(Files.readString(compose));
        Map<String, Object> services = map(document.get("services"));

        assertThat(services).containsKeys("mysql", "redis", "kafka", "user-service", "post-service",
                "comment-service", "like-service", "view-service", "post-read-service", "hot-post-service", "web");
        assertThat(map(services.get("web"))).containsKey("ports");
        services.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("web"))
                .forEach(entry -> assertThat(map(entry.getValue())).doesNotContainKey("ports"));
    }

    @Test
    void springBootContainersUseExecutableJarEntrypoint() throws IOException {
        Path projectRoot = Path.of("..").normalize();
        String dockerfile = Files.readString(projectRoot.resolve("Dockerfile"));
        String composeYaml = Files.readString(projectRoot.resolve("compose.yaml"));

        assertThat(dockerfile)
                .contains("*-plain.jar) continue")
                .contains("COPY --from=builder /workspace/app.jar /app/app.jar")
                .contains("ENTRYPOINT [\"sh\", \"-c\", \"exec java $JAVA_OPTS -jar /app/app.jar\"]")
                .doesNotContain("$MAIN_CLASS");
        assertThat(composeYaml).doesNotContain("MAIN_CLASS");

        Map<String, Object> document = new Yaml().load(composeYaml);
        Map<String, Object> services = map(document.get("services"));
        List.of("user-service", "post-service", "comment-service", "like-service", "view-service",
                        "post-read-service", "hot-post-service", "web")
                .forEach(name -> assertThat(map(services.get(name)))
                        .doesNotContainKeys("command", "entrypoint"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
