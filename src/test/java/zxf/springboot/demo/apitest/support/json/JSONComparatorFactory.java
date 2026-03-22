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
public final class JSONComparatorFactory {

    private JSONComparatorFactory() {
    }

    /**
     * Creates a JSON comparator for API response validation.
     * Ignores dynamic fields like timestamps, IDs, and external task IDs.
     *
     * @return JSONComparator with custom matching rules
     */
    public static JSONComparator buildApiResponseComparator() {
        return new CustomComparator(JSONCompareMode.LENIENT,
                // Ignore timestamp fields
                customization("timestamp"),
                customization("createdAt"),
                customization("updatedAt"),
                customization("created_at"),
                customization("updated_at"),
                // Ignore ID fields (UUIDs generated at runtime)
                customization("id"),
                // Ignore external task ID (dynamic value from downstream service)
                customization("externalTaskId"),
                // Ignore priority (may vary)
                customization("priority")
        );
    }

    private static Customization customization(String fieldName) {
        return Customization.customization(fieldName, new RegularExpressionValueMatcher<>(".*"));
    }
}