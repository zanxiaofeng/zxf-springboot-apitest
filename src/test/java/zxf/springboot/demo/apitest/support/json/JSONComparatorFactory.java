package zxf.springboot.demo.apitest.support.json;

import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

/**
 * Factory for creating JSON comparators with custom matching rules.
 * Useful for ignoring dynamic fields during assertion.
 */
public class JSONComparatorFactory {

    /**
     * Creates a JSON comparator for API response validation.
     * Ignores dynamic fields like timestamps and IDs.
     *
     * @return JSONComparator with custom matching rules
     */
    public static JSONComparator buildApiResponseComparator() {
        // Ignore timestamp fields (matches any value)
        Customization timestamp = Customization.customization("timestamp",
                new RegularExpressionValueMatcher<>(".*"));

        // Ignore any field that contains "At" (createdAt, updatedAt)
        Customization createdAt = Customization.customization("createdAt",
                new RegularExpressionValueMatcher<>(".*"));

        Customization updatedAt = Customization.customization("updatedAt",
                new RegularExpressionValueMatcher<>(".*"));

        // Ignore ID fields (UUID generated)
        Customization id = Customization.customization("id",
                new RegularExpressionValueMatcher<>("[\\w-]+"));

        // Ignore downstream response (dynamic)
        Customization downstreamResponse = Customization.customization("downstreamResponse",
                new RegularExpressionValueMatcher<>(".*"));

        return new CustomComparator(JSONCompareMode.LENIENT,
                timestamp, createdAt, updatedAt, id, downstreamResponse);
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