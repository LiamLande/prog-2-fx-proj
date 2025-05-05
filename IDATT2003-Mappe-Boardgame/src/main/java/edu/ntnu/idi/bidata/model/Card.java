package edu.ntnu.idi.bidata.model;

import com.google.gson.JsonObject;

public class Card {
    private final int id;
    private final String type;
    private final String description;
    private final JsonObject rawData; // Store raw JSON for additional properties

    public Card(int id, String type, String description, JsonObject rawData) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.rawData = rawData;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }

    public boolean hasProperty(String propertyName) {
        return rawData.has(propertyName);
    }

    public int getIntProperty(String propertyName, int defaultValue) {
        return rawData.has(propertyName) ? rawData.get(propertyName).getAsInt() : defaultValue;
    }

    public String getStringProperty(String propertyName, String defaultValue) {
        return rawData.has(propertyName) ? rawData.get(propertyName).getAsString() : defaultValue;
    }
}