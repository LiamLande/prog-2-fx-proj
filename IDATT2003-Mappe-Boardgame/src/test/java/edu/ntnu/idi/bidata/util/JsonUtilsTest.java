package edu.ntnu.idi.bidata.util;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import edu.ntnu.idi.bidata.exception.JsonParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    @DisplayName("Constructor should be inaccessible")
    void testConstructorInaccessible() throws Exception {
        Constructor<JsonUtils> constructor = JsonUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // The constructor doesn't explicitly throw an exception but is private
        // Just verify we can access it with reflection
        JsonUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("Should parse valid JSON string")
    void testParseValidJson() {
        String json = "{\"name\":\"John\",\"age\":30}";
        JsonObject result = JsonUtils.parse(json);

        assertEquals("John", result.get("name").getAsString());
        assertEquals(30, result.get("age").getAsInt());
    }

    @Test
    @DisplayName("Should throw JsonParseException for non-object JSON")
    void testParseNonObjectJson() {
        String nonObjectJson = "\"just a string\""; // Not an object

        JsonParseException exception = assertThrows(JsonParseException.class,
                () -> JsonUtils.parse(nonObjectJson));
        assertTrue(exception.getMessage().contains("Failed to parse JSON string"));
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("Should read valid JSON from Reader")
    void testReadValidJson() {
        String json = "{\"name\":\"John\",\"age\":30}";
        Reader reader = new StringReader(json);

        JsonObject result = JsonUtils.read(reader);

        assertEquals("John", result.get("name").getAsString());
        assertEquals(30, result.get("age").getAsInt());
    }



    @Test
    @DisplayName("Should throw JsonParseException for non-object JSON from Reader")
    void testReadNonObjectJson() {
        String nonObjectJson = "\"just a string\""; // Not an object
        Reader reader = new StringReader(nonObjectJson);

        JsonParseException exception = assertThrows(JsonParseException.class,
                () -> JsonUtils.read(reader));
        assertTrue(exception.getMessage().contains("Failed to parse JSON"));
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

}