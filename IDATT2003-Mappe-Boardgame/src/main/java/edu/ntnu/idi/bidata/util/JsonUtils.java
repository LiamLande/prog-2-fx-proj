package edu.ntnu.idi.bidata.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import edu.ntnu.idi.bidata.exception.JsonParseException;

/**
 * Utility methods for JSON parsing and serialization using Gson.
 */
public final class JsonUtils {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private JsonUtils() { /* prevent instantiation */ }

  /**
   * Parses JSON text into a JsonObject.
   * @param json raw JSON string
   * @return parsed JsonObject
   */
  public static JsonObject parse(String json) {
    try {
      JsonElement elem = JsonParser.parseString(json);
      return elem.getAsJsonObject();
    } catch (JsonSyntaxException | IllegalStateException e) {
      throw new JsonParseException("Failed to parse JSON string", e);
    }
  }

  /**
   * Reads and parses JSON from a Reader into a JsonObject.
   * @param reader source of JSON data
   * @return parsed JsonObject
   */
  public static JsonObject read(Reader reader) {
    try (BufferedReader br = new BufferedReader(reader)) {
      return JsonParser.parseReader(br).getAsJsonObject();
    } catch (IOException e) {
      throw new JsonParseException("I/O error reading JSON data", e);
    } catch (JsonSyntaxException | IllegalStateException e) {
      throw new JsonParseException("Failed to parse JSON from reader", e);
    }
  }

  /**
   * Serializes the JsonObject to a JSON string.
   * @param obj JsonObject to serialize
   * @return JSON text
   */
  public static String toJson(JsonObject obj) {
    return GSON.toJson(obj);
  }

  /**
   * Writes the JsonObject as JSON to the given Writer.
   * @param writer destination for JSON data
   * @param obj JsonObject to serialize
   */
  public static void write(Writer writer, JsonObject obj) {
    try {
      writer.write(GSON.toJson(obj));
      writer.flush();
    } catch (IOException e) {
      throw new JsonParseException("I/O error writing JSON data", e);
    }
  }
}
