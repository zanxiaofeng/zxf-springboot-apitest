package zxf.springboot.demo.apitest.support.json;

import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

/**
 * Factory for creating JSON comparators with custom matching rules.
 * Useful for ignoring dynamic fields like timestamps during assertion.
 */
public class JSONComparatorFactory {

    /**
     * Creates a JSON comparator for API response validation.
     * Ignores dynamic fields: timestamp, currentTimeMillis, and downstream.value
     *
     * @return JSONComparator with custom matching rules
     */
    public static JSONComparator buildApiResponseComparator() {
        // Ignore timestamp field (matches any value)
        Customization timestamp = Customization.customization("timestamp",
                new RegularExpressionValueMatcher<>(".*"));

        // Ignore downstream.value (numeric)
        Customization downstreamValue = Customization.customization("**.downstream.value",
                new RegularExpressionValueMatcher<>("\\d+"));

        // Ignore currentTimeMillis (numeric)
        Customization currentTimeMillis = Customization.customization("currentTimeMillis",
                new RegularExpressionValueMatcher<>("\\d+"));

        // Ignore project.id for dynamic IDs
        Customization projectId = Customization.customization("project.id",
                new RegularExpressionValueMatcher<>("[\\w-]+"));

        return new CustomComparator(JSONCompareMode.LENIENT,
                timestamp, downstreamValue, currentTimeMillis, projectId);
    }

    /**
     * Creates a strict JSON comparator for exact matching.
     *
     * @return JSONComparator with strict matching
     */
    public static JSONComparator buildStrictComparator() {
        return new CustomComparator(JSONCompareMode.STRICT);
    }

    /**
     * Creates a lenient JSON comparator that ignores extra fields.
     *
     * @return JSONComparator with lenient matching
     */
    public static JSONComparator buildLenientComparator() {
        return new CustomComparator(JSONCompareMode.LENIENT);
    }
}