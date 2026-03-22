package zxf.springboot.demo.apitest.support.json;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for loading JSON test data from classpath resources.
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
}