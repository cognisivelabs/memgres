package com.memgres.types.jsonb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a JSONB value with PostgreSQL-compatible operations.
 * This class provides binary JSON storage and supports PostgreSQL JSONB operators.
 */
public class JsonbValue {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final JsonNode jsonNode;
    private final byte[] binaryData;
    
    private JsonbValue(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        this.binaryData = serializeToBytes(jsonNode);
    }
    
    /**
     * Create a JsonbValue from a JSON string
     * @param jsonString the JSON string
     * @return a new JsonbValue
     * @throws IllegalArgumentException if the JSON string is invalid
     */
    public static JsonbValue fromString(String jsonString) {
        if (jsonString == null) {
            return new JsonbValue(NullNode.getInstance());
        }
        
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            return new JsonbValue(node);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON string: " + jsonString, e);
        }
    }
    
    /**
     * Create a JsonbValue from an object
     * @param object the object to convert
     * @return a new JsonbValue
     */
    public static JsonbValue from(Object object) {
        if (object == null) {
            return new JsonbValue(NullNode.getInstance());
        }
        
        if (object instanceof JsonbValue) {
            return (JsonbValue) object;
        }
        
        JsonNode node = objectMapper.valueToTree(object);
        return new JsonbValue(node);
    }
    
    /**
     * Create a JsonbValue from a Map
     * @param map the map to convert
     * @return a new JsonbValue
     */
    public static JsonbValue fromMap(Map<String, Object> map) {
        if (map == null) {
            return new JsonbValue(NullNode.getInstance());
        }
        
        ObjectNode objectNode = objectMapper.createObjectNode();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            JsonNode valueNode = objectMapper.valueToTree(entry.getValue());
            objectNode.set(entry.getKey(), valueNode);
        }
        
        return new JsonbValue(objectNode);
    }
    
    /**
     * Create a JsonbValue from a List
     * @param list the list to convert
     * @return a new JsonbValue
     */
    public static JsonbValue fromList(List<Object> list) {
        if (list == null) {
            return new JsonbValue(NullNode.getInstance());
        }
        
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Object item : list) {
            JsonNode itemNode = objectMapper.valueToTree(item);
            arrayNode.add(itemNode);
        }
        
        return new JsonbValue(arrayNode);
    }
    
    /**
     * Get the JSON node
     * @return the underlying JsonNode
     */
    public JsonNode getJsonNode() {
        return jsonNode;
    }
    
    /**
     * Get the binary representation
     * @return the binary data
     */
    public byte[] getBinaryData() {
        return binaryData.clone();
    }
    
    /**
     * Convert to JSON string
     * @return the JSON string representation
     */
    public String toJsonString() {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }
    
    /**
     * Check if this JSONB value is null
     * @return true if null
     */
    public boolean isNull() {
        return jsonNode.isNull();
    }
    
    /**
     * Check if this JSONB value is an object
     * @return true if object
     */
    public boolean isObject() {
        return jsonNode.isObject();
    }
    
    /**
     * Check if this JSONB value is an array
     * @return true if array
     */
    public boolean isArray() {
        return jsonNode.isArray();
    }
    
    /**
     * Check if this JSONB value is a string
     * @return true if string
     */
    public boolean isString() {
        return jsonNode.isTextual();
    }
    
    /**
     * Check if this JSONB value is a number
     * @return true if number
     */
    public boolean isNumber() {
        return jsonNode.isNumber();
    }
    
    /**
     * Check if this JSONB value is a boolean
     * @return true if boolean
     */
    public boolean isBoolean() {
        return jsonNode.isBoolean();
    }
    
    // PostgreSQL JSONB Operators Implementation
    
    /**
     * Implements the @> operator (contains)
     * @param other the value to check for containment
     * @return true if this contains the other value
     */
    public boolean contains(JsonbValue other) {
        return containsValue(this.jsonNode, other.jsonNode);
    }
    
    /**
     * Implements the <@ operator (contained by)
     * @param other the value to check if this is contained by
     * @return true if this is contained by the other value
     */
    public boolean containedBy(JsonbValue other) {
        return other.contains(this);
    }
    
    /**
     * Implements the ? operator (key exists)
     * @param key the key to check
     * @return true if the key exists (for objects) or value exists (for arrays)
     */
    public boolean hasKey(String key) {
        if (jsonNode.isObject()) {
            return jsonNode.has(key);
        } else if (jsonNode.isArray()) {
            for (JsonNode element : jsonNode) {
                if (element.isTextual() && element.textValue().equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Implements the ?| operator (any key exists)
     * @param keys the keys to check
     * @return true if any of the keys exist
     */
    public boolean hasAnyKey(String... keys) {
        for (String key : keys) {
            if (hasKey(key)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Implements the ?& operator (all keys exist)
     * @param keys the keys to check
     * @return true if all of the keys exist
     */
    public boolean hasAllKeys(String... keys) {
        for (String key : keys) {
            if (!hasKey(key)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Implements the -> operator (get JSON object field)
     * @param key the key to get
     * @return the value at the key, or null if not found
     */
    public JsonbValue getField(String key) {
        if (!jsonNode.isObject()) {
            return new JsonbValue(NullNode.getInstance());
        }
        
        JsonNode fieldNode = jsonNode.get(key);
        return fieldNode != null ? new JsonbValue(fieldNode) : new JsonbValue(NullNode.getInstance());
    }
    
    /**
     * Implements the -> operator for array index (get JSON array element)
     * @param index the array index
     * @return the value at the index, or null if not found
     */
    public JsonbValue getElement(int index) {
        if (!jsonNode.isArray()) {
            return new JsonbValue(NullNode.getInstance());
        }
        
        JsonNode elementNode = jsonNode.get(index);
        return elementNode != null ? new JsonbValue(elementNode) : new JsonbValue(NullNode.getInstance());
    }
    
    /**
     * Implements the ->> operator (get JSON object field as text)
     * @param key the key to get
     * @return the text value at the key, or null if not found
     */
    public String getFieldAsText(String key) {
        JsonbValue field = getField(key);
        return field.isNull() ? null : field.asText();
    }
    
    /**
     * Implements the ->> operator for array index (get JSON array element as text)
     * @param index the array index
     * @return the text value at the index, or null if not found
     */
    public String getElementAsText(int index) {
        JsonbValue element = getElement(index);
        return element.isNull() ? null : element.asText();
    }
    
    /**
     * Implements the #> operator (get JSON object at path)
     * @param path the path as an array of keys/indices
     * @return the value at the path, or null if not found
     */
    public JsonbValue getPath(String... path) {
        JsonNode current = jsonNode;
        
        for (String pathElement : path) {
            if (current.isObject()) {
                current = current.get(pathElement);
            } else if (current.isArray()) {
                try {
                    int index = Integer.parseInt(pathElement);
                    current = current.get(index);
                } catch (NumberFormatException e) {
                    return new JsonbValue(NullNode.getInstance());
                }
            } else {
                return new JsonbValue(NullNode.getInstance());
            }
            
            if (current == null) {
                return new JsonbValue(NullNode.getInstance());
            }
        }
        
        return new JsonbValue(current);
    }
    
    /**
     * Implements the #>> operator (get JSON object at path as text)
     * @param path the path as an array of keys/indices
     * @return the text value at the path, or null if not found
     */
    public String getPathAsText(String... path) {
        JsonbValue pathValue = getPath(path);
        return pathValue.isNull() ? null : pathValue.asText();
    }
    
    /**
     * Convert to text value
     * @return the text representation
     */
    public String asText() {
        if (jsonNode.isTextual()) {
            return jsonNode.textValue();
        } else if (jsonNode.isNull()) {
            return null;
        } else {
            return jsonNode.toString();
        }
    }
    
    /**
     * Get size (number of top-level elements)
     * @return the size
     */
    public int size() {
        if (jsonNode.isObject()) {
            return jsonNode.size();
        } else if (jsonNode.isArray()) {
            return jsonNode.size();
        } else {
            return jsonNode.isNull() ? 0 : 1;
        }
    }
    
    private static boolean containsValue(JsonNode container, JsonNode contained) {
        if (container.equals(contained)) {
            return true;
        }
        
        if (container.isObject() && contained.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = contained.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode containerField = container.get(field.getKey());
                if (containerField == null || !containsValue(containerField, field.getValue())) {
                    return false;
                }
            }
            return true;
        }
        
        if (container.isArray() && contained.isArray()) {
            for (JsonNode containedElement : contained) {
                boolean found = false;
                for (JsonNode containerElement : container) {
                    if (containsValue(containerElement, containedElement)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        
        if (container.isArray()) {
            for (JsonNode element : container) {
                if (containsValue(element, contained)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static byte[] serializeToBytes(JsonNode node) {
        try {
            return objectMapper.writeValueAsBytes(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON to bytes", e);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        JsonbValue that = (JsonbValue) o;
        return Objects.equals(jsonNode, that.jsonNode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jsonNode);
    }
    
    @Override
    public String toString() {
        return toJsonString();
    }
}