package edu.ntnu.idi.bidata.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser; // For creating complex JsonObject for testing
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

  private static Card cardWithFullData;
  private static Card cardWithNullData;
  private static Card cardWithEmptyData;

  private static final int DEFAULT_ID = 1;
  private static final String DEFAULT_TYPE = "TestType";
  private static final String DEFAULT_DESCRIPTION = "TestDescription";
  private static final int INT_DEFAULT_VAL = -1;
  private static final String STRING_DEFAULT_VAL = "default";

  @BeforeAll // Use BeforeAll if setup is static and doesn't change per test
  static void setUpCards() {
    JsonObject fullRaw = new JsonObject();
    fullRaw.addProperty("intKey", 123);
    fullRaw.addProperty("stringKey", "hello");
    fullRaw.addProperty("booleanKey", true);
    fullRaw.addProperty("doubleKey", 45.67);
    fullRaw.add("objectKey", JsonParser.parseString("{\"nested\": true}").getAsJsonObject());


    cardWithFullData = new Card(DEFAULT_ID, DEFAULT_TYPE, DEFAULT_DESCRIPTION, fullRaw);
    cardWithNullData = new Card(DEFAULT_ID + 1, "NullDataCard", "Card with null raw data", null);
    cardWithEmptyData = new Card(DEFAULT_ID + 2, "EmptyDataCard", "Card with empty raw data", new JsonObject());
  }

  @Test
  @DisplayName("Basic Getters should return constructor values")
  void testBasicGetters() {
    assertEquals(DEFAULT_ID, cardWithFullData.getId(), "getId() failed");
    assertEquals(DEFAULT_TYPE, cardWithFullData.getType(), "getType() failed");
    assertEquals(DEFAULT_DESCRIPTION, cardWithFullData.getDescription(), "getDescription() failed");

    // Call getters on other card instances too to ensure they don't crash
    assertNotNull(cardWithNullData.getType());
    assertNotNull(cardWithEmptyData.getType());
  }

  @Test
  @DisplayName("hasProperty covers all its branches")
  void testHasProperty_BranchCoverage() {
    // Path 1: rawData is null
    assertFalse(cardWithNullData.hasProperty("anyKey"), "hasProperty should return false for null rawData");

    // Path 2: propertyName is null (rawData is not null)
    assertFalse(cardWithFullData.hasProperty(null), "hasProperty should return false for null propertyName");

    // Path 3: property exists
    assertTrue(cardWithFullData.hasProperty("intKey"), "hasProperty should find existing key");

    // Path 4: property does not exist (rawData is not null, propertyName is not null)
    assertFalse(cardWithFullData.hasProperty("nonExistentKey"), "hasProperty should not find non-existent key");
    assertFalse(cardWithEmptyData.hasProperty("anyKey"), "hasProperty should return false for key in empty rawData");
  }

  @Test
  @DisplayName("getIntProperty covers all its branches")
  void testGetIntProperty_BranchCoverage() {
    // Path 1: !hasProperty(propertyName) is true (e.g., rawData is null)
    assertEquals(INT_DEFAULT_VAL, cardWithNullData.getIntProperty("intKey", INT_DEFAULT_VAL), "getIntProperty for null rawData");
    // Path 1: !hasProperty(propertyName) is true (e.g., propertyName is null)
    assertEquals(INT_DEFAULT_VAL, cardWithFullData.getIntProperty(null, INT_DEFAULT_VAL), "getIntProperty for null propertyName");
    // Path 1: !hasProperty(propertyName) is true (e.g., key doesn't exist)
    assertEquals(INT_DEFAULT_VAL, cardWithFullData.getIntProperty("nonExistentKey", INT_DEFAULT_VAL), "getIntProperty for non-existent key");

    // Property exists from here on
    // Path 2: Not a primitive (it's a JsonObject)
    assertEquals(INT_DEFAULT_VAL, cardWithFullData.getIntProperty("objectKey", INT_DEFAULT_VAL), "getIntProperty for object type");

    // Path 3: Primitive, but not a number (it's a string)
    assertEquals(INT_DEFAULT_VAL, cardWithFullData.getIntProperty("stringKey", INT_DEFAULT_VAL), "getIntProperty for string type");
    // Path 3: Primitive, but not a number (it's a boolean)
    assertEquals(INT_DEFAULT_VAL, cardWithFullData.getIntProperty("booleanKey", INT_DEFAULT_VAL), "getIntProperty for boolean type");

    // It's a number primitive
    // Path 4a: Is an int, getAsInt succeeds
    assertEquals(123, cardWithFullData.getIntProperty("intKey", INT_DEFAULT_VAL), "getIntProperty for actual int");

    // Path 4b: Is a double/decimal, should return default
    assertEquals(INT_DEFAULT_VAL, cardWithFullData.getIntProperty("doubleKey", INT_DEFAULT_VAL), "getIntProperty for double type");

    // Path 5 (NumberFormatException): This is harder to reliably trigger with JsonPrimitive's getAsInt
    // unless the number is truly malformed in a way JsonPrimitive allows but getAsInt rejects (e.g. "123L" if it were parsed as number).
    // For typical JSON, if it's a number, getAsInt usually works or it falls into the double case.
    // If you had a specific JSON that could cause NFE after `isNumber()` is true, you'd add it.
    // For now, the doubleKey test covers a non-integer number.
    JsonObject trickyNumber = new JsonObject();
    // Gson's JsonPrimitive.getAsInt() is quite robust. It can parse "123" as int.
    // A direct NumberFormatException for a valid JsonPrimitive(Number) is rare.
    // The "doubleKey" test implicitly tests the robustness of getAsInt for non-integer numbers.
  }

  @Test
  @DisplayName("getStringProperty covers all its branches")
  void testGetStringProperty_BranchCoverage() {
    // Path 1: !hasProperty(propertyName) is true (e.g., rawData is null)
    assertEquals(STRING_DEFAULT_VAL, cardWithNullData.getStringProperty("stringKey", STRING_DEFAULT_VAL), "getStringProperty for null rawData");
    // Path 1: !hasProperty(propertyName) is true (e.g., propertyName is null)
    assertEquals(STRING_DEFAULT_VAL, cardWithFullData.getStringProperty(null, STRING_DEFAULT_VAL), "getStringProperty for null propertyName");
    // Path 1: !hasProperty(propertyName) is true (e.g., key doesn't exist)
    assertEquals(STRING_DEFAULT_VAL, cardWithFullData.getStringProperty("nonExistentKey", STRING_DEFAULT_VAL), "getStringProperty for non-existent key");

    // Property exists from here on
    // Path 2: Not a primitive (it's a JsonObject)
    assertEquals(STRING_DEFAULT_VAL, cardWithFullData.getStringProperty("objectKey", STRING_DEFAULT_VAL), "getStringProperty for object type");

    // Path 3: Primitive, but not a string (it's an int)
    assertEquals(STRING_DEFAULT_VAL, cardWithFullData.getStringProperty("intKey", STRING_DEFAULT_VAL), "getStringProperty for int type");
    // Path 3: Primitive, but not a string (it's a boolean)
    assertEquals(STRING_DEFAULT_VAL, cardWithFullData.getStringProperty("booleanKey", STRING_DEFAULT_VAL), "getStringProperty for boolean type");
    // Path 3: Primitive, but not a string (it's a double)
    assertEquals(STRING_DEFAULT_VAL, cardWithFullData.getStringProperty("doubleKey", STRING_DEFAULT_VAL), "getStringProperty for double type");

    // Path 4: Is a string primitive
    assertEquals("hello", cardWithFullData.getStringProperty("stringKey", STRING_DEFAULT_VAL), "getStringProperty for actual string");
  }
}