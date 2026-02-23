package edts.week8_practice1.dto.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic filter request DTO.
 * Supports key-value pair filtering.
 */
public class FilterRequest {

    private Map<String, String> filters = new HashMap<>();

    public FilterRequest() {
    }

    public FilterRequest(Map<String, String> filters) {
        this.filters = filters != null ? filters : new HashMap<>();
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters != null ? filters : new HashMap<>();
    }

    public void addFilter(String key, String value) {
        this.filters.put(key, value);
    }

    public String getFilterValue(String key) {
        return filters.get(key);
    }

    public boolean hasFilter(String key) {
        return filters.containsKey(key) && filters.get(key) != null;
    }

    public Boolean getBooleanFilter(String key) {
        String value = getFilterValue(key);
        if (value == null) return null;
        return Boolean.parseBoolean(value);
    }

    public Integer getIntegerFilter(String key) {
        String value = getFilterValue(key);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
