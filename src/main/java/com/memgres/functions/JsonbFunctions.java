package com.memgres.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.memgres.types.jsonb.JsonbValue;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Enhanced JSONB functions for MemGres database.
 * Provides advanced PostgreSQL-compatible JSONB operations including JSONPath and aggregation functions.
 */
public class JsonbFunctions {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Simple JSONPath implementation for basic path queries.
     * Supports basic path syntax like $.key, $.array[*], $.nested.key
     * @param jsonb the JSONB value to query
     * @param jsonPath the JSONPath expression
     * @return array of matching values
     */
    public static JsonbValue[] jsonbPathQuery(JsonbValue jsonb, String jsonPath) {
        if (jsonb == null || jsonPath == null) {
            return new JsonbValue[0];
        }
        
        List<JsonbValue> results = new ArrayList<>();
        evaluateJsonPath(jsonb.getJsonNode(), jsonPath, results);
        return results.toArray(new JsonbValue[0]);
    }
    
    /**
     * Extract all values from JSONB object or array (recursive).
     * @param jsonb the JSONB value to extract from
     * @return array of all values
     */
    public static JsonbValue[] jsonbEachRecursive(JsonbValue jsonb) {
        if (jsonb == null) {
            return new JsonbValue[0];
        }
        
        List<JsonbValue> results = new ArrayList<>();
        extractAllValues(jsonb.getJsonNode(), results);
        return results.toArray(new JsonbValue[0]);
    }
    
    /**
     * Get all keys from JSONB object.
     * @param jsonb the JSONB object
     * @return array of keys
     */
    public static String[] jsonbObjectKeys(JsonbValue jsonb) {
        if (jsonb == null || !jsonb.isObject()) {
            return new String[0];
        }
        
        List<String> keys = new ArrayList<>();
        jsonb.getJsonNode().fieldNames().forEachRemaining(keys::add);
        return keys.toArray(new String[0]);
    }
    
    /**
     * Get array length of JSONB array.
     * @param jsonb the JSONB array
     * @return the length of the array, or null if not an array
     */
    public static Integer jsonbArrayLength(JsonbValue jsonb) {
        if (jsonb == null || !jsonb.isArray()) {
            return null;
        }
        
        return jsonb.size();
    }
    
    /**
     * Extract path from JSONB as text array.
     * @param jsonb the JSONB value
     * @param path the path elements
     * @return the value at path as text, or null if not found
     */
    public static String jsonbExtractPathText(JsonbValue jsonb, String... path) {
        if (jsonb == null) {
            return null;
        }
        
        return jsonb.getPathAsText(path);
    }
    
    /**
     * Build JSONB object from key-value pairs.
     * @param pairs alternating keys and values
     * @return new JSONB object
     */
    public static JsonbValue jsonbBuildObject(Object... pairs) {
        if (pairs == null) {
            throw new IllegalArgumentException("jsonb_build_object requires non-null arguments");
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("jsonb_build_object requires even number of arguments");
        }
        
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            String key = pairs[i] != null ? pairs[i].toString() : null;
            Object value = pairs[i + 1];
            if (key != null) {
                map.put(key, value);
            }
        }
        
        return JsonbValue.fromMap(map);
    }
    
    /**
     * Build JSONB array from values.
     * @param values the values to include in the array
     * @return new JSONB array
     */
    public static JsonbValue jsonbBuildArray(Object... values) {
        if (values == null) {
            return JsonbValue.fromList(new ArrayList<>());
        }
        
        return JsonbValue.fromList(Arrays.asList(values));
    }
    
    /**
     * Aggregate JSONB values into a single array.
     * @param values list of JSONB values to aggregate
     * @return aggregated JSONB array
     */
    public static JsonbValue jsonbAgg(List<JsonbValue> values) {
        if (values == null) {
            return JsonbValue.fromList(new ArrayList<>());
        }
        
        List<Object> aggregated = values.stream()
                .filter(Objects::nonNull)
                .map(jsonb -> {
                    try {
                        return jsonb.getJsonNode();
                    } catch (Exception e) {
                        return jsonb.toString();
                    }
                })
                .collect(Collectors.toList());
        
        return JsonbValue.fromList(aggregated);
    }
    
    /**
     * Aggregate JSONB objects into a single object.
     * @param values list of JSONB objects to aggregate
     * @return aggregated JSONB object
     */
    public static JsonbValue jsonbObjectAgg(List<JsonbValue> values) {
        if (values == null) {
            return JsonbValue.fromMap(new HashMap<>());
        }
        
        Map<String, Object> aggregated = new HashMap<>();
        for (JsonbValue value : values) {
            if (value != null && value.isObject()) {
                value.getJsonNode().fields().forEachRemaining(entry -> 
                    aggregated.put(entry.getKey(), entry.getValue()));
            }
        }
        
        return JsonbValue.fromMap(aggregated);
    }
    
    /**
     * Remove key from JSONB object.
     * @param jsonb the source JSONB object
     * @param key the key to remove
     * @return new JSONB object without the key
     */
    public static JsonbValue jsonbRemoveKey(JsonbValue jsonb, String key) {
        if (jsonb == null || !jsonb.isObject() || key == null) {
            return jsonb;
        }
        
        ObjectNode newObject = jsonb.getJsonNode().deepCopy();
        if (newObject.isObject()) {
            ((ObjectNode) newObject).remove(key);
        }
        
        return JsonbValue.from(newObject);
    }
    
    /**
     * Set value at path in JSONB.
     * @param jsonb the source JSONB value
     * @param path the path to set
     * @param value the value to set
     * @return new JSONB with value set at path
     */
    public static JsonbValue jsonbSetPath(JsonbValue jsonb, String[] path, JsonbValue value) {
        if (jsonb == null || path == null || path.length == 0 || value == null) {
            return jsonb;
        }
        
        JsonNode result = jsonb.getJsonNode().deepCopy();
        result = setPathValue(result, path, 0, value.getJsonNode());
        
        return JsonbValue.from(result);
    }
    
    /**
     * Check if JSONB value matches a pattern (simple pattern matching).
     * @param jsonb the JSONB value to check
     * @param pattern the pattern to match against
     * @return true if the pattern matches
     */
    public static boolean jsonbMatches(JsonbValue jsonb, String pattern) {
        if (jsonb == null || pattern == null) {
            return false;
        }
        
        String jsonString = jsonb.toString();
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(jsonString).find();
    }
    
    /**
     * Get the type of a JSONB value.
     * @param jsonb the JSONB value
     * @return the type as a string ("object", "array", "string", "number", "boolean", "null")
     */
    public static String jsonbTypeof(JsonbValue jsonb) {
        if (jsonb == null) {
            return "null";
        }
        
        JsonNode node = jsonb.getJsonNode();
        if (node.isObject()) {
            return "object";
        } else if (node.isArray()) {
            return "array";
        } else if (node.isTextual()) {
            return "string";
        } else if (node.isNumber()) {
            return "number";
        } else if (node.isBoolean()) {
            return "boolean";
        } else {
            return "null";
        }
    }
    
    /**
     * Pretty print JSONB with formatting.
     * @param jsonb the JSONB value to format
     * @return formatted JSON string
     */
    public static String jsonbPretty(JsonbValue jsonb) {
        if (jsonb == null) {
            return null;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonb.getJsonNode());
        } catch (Exception e) {
            return jsonb.toString();
        }
    }
    
    /**
     * Strip null values from JSONB object or array.
     * @param jsonb the JSONB value to strip nulls from
     * @return new JSONB value without null values
     */
    public static JsonbValue jsonbStripNulls(JsonbValue jsonb) {
        if (jsonb == null) {
            return null;
        }
        
        JsonNode stripped = stripNullValues(jsonb.getJsonNode());
        return JsonbValue.from(stripped);
    }
    
    private static void evaluateJsonPath(JsonNode node, String path, List<JsonbValue> results) {
        // Simple JSONPath implementation - supports basic syntax
        if (path.equals("$")) {
            results.add(JsonbValue.from(node));
            return;
        }
        
        if (path.startsWith("$.")) {
            String[] parts = path.substring(2).split("\\.");
            JsonNode current = node;
            
            for (String part : parts) {
                if (part.equals("*")) {
                    // Wildcard - get all values
                    if (current.isObject()) {
                        current.fields().forEachRemaining(entry -> 
                            results.add(JsonbValue.from(entry.getValue())));
                    } else if (current.isArray()) {
                        for (JsonNode element : current) {
                            results.add(JsonbValue.from(element));
                        }
                    }
                    return;
                } else if (part.matches("\\w+\\[\\*\\]")) {
                    // Array wildcard like "array[*]"
                    String fieldName = part.substring(0, part.indexOf('['));
                    if (current.isObject() && current.has(fieldName)) {
                        JsonNode arrayNode = current.get(fieldName);
                        if (arrayNode.isArray()) {
                            for (JsonNode element : arrayNode) {
                                results.add(JsonbValue.from(element));
                            }
                        }
                    }
                    return;
                } else if (part.matches("\\w+\\[\\d+\\]")) {
                    // Array index like "array[0]"
                    String fieldName = part.substring(0, part.indexOf('['));
                    String indexStr = part.substring(part.indexOf('[') + 1, part.indexOf(']'));
                    int index = Integer.parseInt(indexStr);
                    
                    if (current.isObject() && current.has(fieldName)) {
                        JsonNode arrayNode = current.get(fieldName);
                        if (arrayNode.isArray() && index < arrayNode.size()) {
                            current = arrayNode.get(index);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    // Regular field access
                    if (current.isObject() && current.has(part)) {
                        current = current.get(part);
                    } else {
                        return;
                    }
                }
            }
            
            results.add(JsonbValue.from(current));
        }
    }
    
    private static void extractAllValues(JsonNode node, List<JsonbValue> results) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                results.add(JsonbValue.from(entry.getValue()));
                extractAllValues(entry.getValue(), results);
            });
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                results.add(JsonbValue.from(element));
                extractAllValues(element, results);
            }
        } else {
            results.add(JsonbValue.from(node));
        }
    }
    
    private static JsonNode setPathValue(JsonNode node, String[] path, int pathIndex, JsonNode value) {
        if (pathIndex >= path.length) {
            return value;
        }
        
        String currentKey = path[pathIndex];
        
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonNode currentValue = objectNode.get(currentKey);
            if (currentValue == null) {
                // Create intermediate objects/arrays as needed
                if (pathIndex < path.length - 1) {
                    currentValue = objectMapper.createObjectNode();
                } else {
                    currentValue = NullNode.getInstance();
                }
            }
            JsonNode newValue = setPathValue(currentValue, path, pathIndex + 1, value);
            objectNode.set(currentKey, newValue);
            return objectNode;
        } else if (node.isArray()) {
            try {
                int index = Integer.parseInt(currentKey);
                ArrayNode arrayNode = (ArrayNode) node;
                // Extend array if necessary
                while (arrayNode.size() <= index) {
                    arrayNode.add(NullNode.getInstance());
                }
                JsonNode currentValue = arrayNode.get(index);
                JsonNode newValue = setPathValue(currentValue, path, pathIndex + 1, value);
                arrayNode.set(index, newValue);
                return arrayNode;
            } catch (NumberFormatException e) {
                // Invalid array index
                return node;
            }
        } else {
            // Replace non-container with object or array
            if (pathIndex < path.length - 1) {
                try {
                    Integer.parseInt(currentKey);
                    // Next key is numeric, create array
                    ArrayNode newArray = objectMapper.createArrayNode();
                    return setPathValue(newArray, path, pathIndex, value);
                } catch (NumberFormatException e) {
                    // Next key is string, create object
                    ObjectNode newObject = objectMapper.createObjectNode();
                    return setPathValue(newObject, path, pathIndex, value);
                }
            } else {
                return value;
            }
        }
    }
    
    private static JsonNode stripNullValues(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                if (!entry.getValue().isNull()) {
                    JsonNode strippedValue = stripNullValues(entry.getValue());
                    objectNode.set(entry.getKey(), strippedValue);
                }
            });
            return objectNode;
        } else if (node.isArray()) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (JsonNode element : node) {
                if (!element.isNull()) {
                    JsonNode strippedElement = stripNullValues(element);
                    arrayNode.add(strippedElement);
                }
            }
            return arrayNode;
        } else {
            return node;
        }
    }
}