package edu.ntnu.idi.bidata.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Card {
    private final int id;
    private final String type;
    private final String description;
    private final JsonObject rawData;

    public Card(int id, String type, String description, JsonObject rawData) {
        this.id = id;
        this.type = type; // Consider null/blank checks
        this.description = description; // Consider null check
        this.rawData = rawData; // Can be null
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }

    public boolean hasProperty(String propertyName) {
        if (rawData == null || propertyName == null) {
            return false;
        }
        return rawData.has(propertyName);
    }

    public int getIntProperty(String propertyName, int defaultValue) {
        // Check if the property exists and rawData is valid
        // This relies on your existing hasProperty logic or direct checks.
        if (this.rawData == null || propertyName == null || !this.rawData.has(propertyName)) {
            return defaultValue;
        }

        JsonElement element = this.rawData.get(propertyName);

        // Check if the element is a JsonPrimitive and represents a number
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()) {
            return defaultValue;
        }

        Number numberValue = primitive.getAsNumber();
        double dValue = numberValue.doubleValue();

        // Check if the double value has a fractional part.
        // Also handles NaN and Infinity, as (dValue % 1) would be NaN for them, and NaN != 0.
        if (dValue % 1 != 0) {
            return defaultValue; // It's a number with a fractional part (e.g., 45.67) or NaN/Infinity
        }

        // At this point, dValue is a whole number (e.g., 45.0 or 123.0).
        // Now, check if it fits within the Java int range.
        // Casting dValue to long is safe here because it's confirmed to be a whole number.
        long lValue = (long) dValue;
        if (lValue >= Integer.MIN_VALUE && lValue <= Integer.MAX_VALUE) {
            return (int) lValue; // Safe to cast to int
        } else {
            // It's a whole number, but it's outside the int range
            return defaultValue;
        }
    }


    public String getStringProperty(String propertyName, String defaultValue) {
        if (!hasProperty(propertyName)) { // This checks rawData != null, propertyName != null, and rawData.has(propertyName)
            return defaultValue;
        }
        // Property exists, now check its type
        if (!rawData.get(propertyName).isJsonPrimitive()) { // e.g. it's a JsonObject or JsonArray
            return defaultValue;
        }
        JsonPrimitive primitive = rawData.get(propertyName).getAsJsonPrimitive();
        if (!primitive.isString()) { // e.g. it's a number or boolean primitive
            return defaultValue;
        }
        return primitive.getAsString();
    }
}