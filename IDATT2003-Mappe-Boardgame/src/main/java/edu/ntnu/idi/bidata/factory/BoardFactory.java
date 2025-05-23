package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.file.BoardJsonReaderWriter;
import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.exception.JsonParseException;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderPlayerSetupScene; // Import Theme enum

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Factory class for creating Board objects.
 * This class provides static methods to instantiate Board objects, primarily by loading them from JSON resources.
 * It is a final class with a private constructor to prevent instantiation.
 */
/**
 * A factory for creating {@link Board} instances.
 * This utility class provides methods to load board configurations from JSON files.
 * It is not meant to be instantiated.
 */
public final class BoardFactory {
  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private BoardFactory() { }

  /**
   * Loads a {@link Board} from a JSON resource file located on the classpath.
   * This method uses {@link BoardJsonReaderWriter} to parse the JSON file and construct the Board object.
   *
   * @param resourcePath The path to the JSON file within the classpath (e.g., "/data/boards/monopoly.json").
   * @param variant The {@link GameVariant} for which the board is being created. This can influence how the JSON is parsed or validated.
   * @param theme The {@link SnakeLadderPlayerSetupScene.Theme} to be applied to the board (currently unused in this method but kept for future compatibility).
   * @return A {@link Board} object deserialized from the specified JSON resource.
   * @throws JsonParseException If an error occurs during JSON parsing or if the resource cannot be found/read.
   *                            This wraps the underlying {@link Exception} that occurred.
   */
  public static Board createFromJson(String resourcePath, GameVariant variant, SnakeLadderPlayerSetupScene.Theme theme) {
    try (Reader reader = new InputStreamReader(
        Objects.requireNonNull(BoardFactory.class.getResourceAsStream(resourcePath)),
        StandardCharsets.UTF_8)) {
      return BoardJsonReaderWriter.read(reader, variant);
    } catch (Exception e) {
      throw new JsonParseException("Failed to load board from JSON: " + resourcePath, e);
    }
  }
}