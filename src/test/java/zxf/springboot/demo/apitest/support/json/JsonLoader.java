package zxf.springboot.demo.apitest.support.json;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class for loading JSON test data from classpath resources.
 * Supports template variables using ${variable} syntax.
 */
public final class JsonLoader {

    private JsonLoader() {
    }

    /**
     * Load JSON content from classpath resource.
     *
     * @param resourcePath path relative to test-data directory (e.g., "project/get-project-by-id.json")
     * @return JSON content as String
     */
    public static String load(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource("test-data/" + resourcePath);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load test data: " + resourcePath, e);
        }
    }

    /**
     * Load JSON content and replace template variables.
     * Variables use ${variableName} syntax.
     *
     * @param resourcePath path relative to test-data directory
     * @param variables    map of variable names to values
     * @return JSON content with variables replaced
     */
    public static String load(String resourcePath, Map<String, String> variables) {
        String content = load(resourcePath);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return content;
    }
}