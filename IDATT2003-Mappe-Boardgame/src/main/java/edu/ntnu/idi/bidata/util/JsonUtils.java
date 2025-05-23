package edu.ntnu.idi.bidata.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.Reader;
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
  public static JsonObject read(Reader reader) throws JsonParseException {
    if (reader == null) {
      // This line ensures the correct exception is thrown
      throw new JsonParseException("Reader cannot be null");
    }
    try {
      return JsonParser.parseReader(reader).getAsJsonObject();
    } catch (JsonIOException | JsonSyntaxException | IllegalStateException e) {
      throw new JsonParseException("Failed to parse JSON: " + e.getMessage(), e);
    }
  }
}
