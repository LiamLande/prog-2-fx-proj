package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.file.BoardJsonReaderWriter;
import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.exception.JsonParseException;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Factory for creating Board instances, optionally from JSON configurations.
 */
public final class BoardFactory {
  private BoardFactory() { /* non-instantiable */ }

  /**
   * Loads a Board from a JSON resource on the classpath.
   * @param resourcePath path to .json file (e.g. "/data/boards/snakes_and_ladders.json")
   * @return deserialized Board
   */
  public static Board createFromJson(String resourcePath, GameVariant variant) {
    try (Reader reader = new InputStreamReader(
        BoardFactory.class.getResourceAsStream(resourcePath),
        StandardCharsets.UTF_8)) {
      return BoardJsonReaderWriter.read(reader, variant);
    } catch (Exception e) {
      throw new JsonParseException("Failed to load board from JSON: " + resourcePath, e);
    }
  }

}