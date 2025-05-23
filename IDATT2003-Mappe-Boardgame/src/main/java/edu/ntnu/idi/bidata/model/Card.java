package edu.ntnu.idi.bidata.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Represents a game card with an ID, type, description, and raw JSON data.
 * This class allows for flexible card data through its rawData field, which can store various properties.
 */
public class Card {
    private final int id;
    private final String type;
    private final String description;
    private final JsonObject rawData;

    /**
     * Constructs a new Card.
     *
     * @param id The unique identifier for the card.
     * @param type The type of the card (e.g., "Chance", "Community Chest").
     * @param description A textual description of the card's effect or content.
     * @param rawData A JsonObject containing additional, type-specific data for the card. Can be null.
     */
    public Card(int id, String type, String description, JsonObject rawData) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.rawData = rawData; // Can be null
    }

    /**
     * Gets the ID of the card.
     *
     * @return The card ID.
     */
    public int getId() { return id; }

    /**
     * Gets the type of the card.
     *
     * @return The card type.
     */
    public String getType() { return type; }

    /**
     * Gets the description of the card.
     *
     * @return The card description.
     */
    public String getDescription() { return description; }

    /**
     * Checks if the card's raw data contains a specific property.
     *
     * @param propertyName The name of the property to check for.
     * @return true if the property exists and rawData is not null, false otherwise.
     */
    public boolean hasProperty(String propertyName) {
        if (rawData == null || propertyName == null) {
            return false;
        }
        return rawData.has(propertyName);
    }

    /**
     * Retrieves an integer property from the card's raw data.
     * Returns a default value if the property does not exist, is not a number, or cannot be safely converted to an int.
     *
     * @param propertyName The name of the integer property.
     * @param defaultValue The value to return if the property is not found or is invalid.
     * @return The integer value of the property, or the defaultValue.
     */
    public int getIntProperty(String propertyName, int defaultValue) {
        // Check if the property exists and rawData is valid
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
        if (dValue % 1 != 0) {
            return defaultValue; // It's a number with a fractional part (e.g., 45.67) or NaN/Infinity
        }
        long lValue = (long) dValue;
        if (lValue >= Integer.MIN_VALUE && lValue <= Integer.MAX_VALUE) {
            return (int) lValue; // Safe to cast to int
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieves a string property from the card's raw data.
     * Returns a default value if the property does not exist or is not a string.
     *
     * @param propertyName The name of the string property.
     * @param defaultValue The value to return if the property is not found or is not a string.
     * @return The string value of the property, or the defaultValue.
     */
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