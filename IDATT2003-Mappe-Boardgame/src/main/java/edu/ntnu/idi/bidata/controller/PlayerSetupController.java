package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.file.PlayerCsvReaderWriter;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.ui.PlayerSetupData;
import edu.ntnu.idi.bidata.util.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the saving and loading of player setup configurations.
 * This class interacts with the file system to persist and retrieve player data,
 * converting between {@link PlayerSetupData} used by the UI and {@link Player} domain objects.
 */
public class PlayerSetupController {

  /**
   * Saves the player setup data to the specified file.
   * The data is converted from a list of {@link PlayerSetupData} objects to {@link Player} objects
   * before being written to the file using {@link PlayerCsvReaderWriter}.
   *
   * @param playerInputs A list of {@link PlayerSetupData} representing the player information to save.
   *                     Cannot be null or empty.
   * @param file The {@link File} to which the player setup will be saved. Cannot be null.
   * @return {@code true} if the player setup was successfully saved.
   * @throws IOException If an I/O error occurs during writing to the file.
   * @throws InvalidParameterException If any of the player input data is invalid,
   *                                   or if issues arise during CSV writing.
   * @throws IllegalArgumentException If {@code playerInputs} is null/empty or {@code file} is null.
   */
  public boolean savePlayerSetup(List<PlayerSetupData> playerInputs, File file) throws IOException, InvalidParameterException {
    Logger.info("Attempting to save player setup.");
    if (playerInputs == null || playerInputs.isEmpty()) {
      Logger.warning("Save player setup request with no player inputs. Aborting save.");
      throw new IllegalArgumentException("Player inputs cannot be null or empty.");
    }
    if (file == null) {
      Logger.error("File cannot be null for saving player setup.");
      throw new IllegalArgumentException("File cannot be null for saving.");
    }
    Logger.debug("Saving " + playerInputs.size() + " player(s) to file: " + file.getAbsolutePath());

    List<Player> playerList = new ArrayList<>();
    Tile placeholderStartTile = new Tile(0); // Placeholder for start tile

    for (PlayerSetupData input : playerInputs) {
      try {
        Logger.debug("Creating Player object for: " + input.name() + " with piece: " + input.pieceIdentifier());
        playerList.add(new Player(input.name(), placeholderStartTile, input.pieceIdentifier()));
      } catch (InvalidParameterException e) {
        Logger.error("Failed to create Player object for '" + input.name() + "' due to invalid parameters.", e);
        throw e; // Re-throw to inform the caller
      }
    }

    try (Writer writer = new FileWriter(file)) {
      PlayerCsvReaderWriter.writeAll(writer, playerList);
      Logger.info("Player setup successfully saved to: " + file.getAbsolutePath());
      return true;
    } catch (IOException e) {
      Logger.error("IOException occurred while saving player setup to " + file.getAbsolutePath(), e);
      throw e;
    } catch (InvalidParameterException e) { // Should not happen if playerList is not null
      Logger.error("InvalidParameterException from PlayerCsvReaderWriter.writeAll (unexpected).", e);
      throw e;
    }
  }

  /**
   * Loads player setup data from the specified file.
   * The data is read from the file using {@link PlayerCsvReaderWriter} and converted
   * from a list of {@link Player} objects to {@link PlayerSetupData} objects.
   * If the file does not exist or cannot be read, an empty list is returned.
   *
   * @param file The {@link File} from which to load the player setup. Cannot be null.
   * @return A list of {@link PlayerSetupData} representing the loaded player information.
   *         Returns an empty list if the file doesn't exist or is unreadable.
   * @throws IOException If an I/O error occurs during reading from the file.
   * @throws InvalidParameterException If the data in the file is malformed or invalid.
   * @throws IllegalArgumentException If {@code file} is null.
   */
  public List<PlayerSetupData> loadPlayerSetup(File file) throws IOException, InvalidParameterException {
    Logger.info("Attempting to load player setup from file.");
    if (file == null) {
      Logger.error("File cannot be null for loading player setup.");
      throw new IllegalArgumentException("File cannot be null for loading.");
    }
    Logger.debug("Loading player setup from: " + file.getAbsolutePath());

    if (!file.exists() || !file.canRead()) {
      Logger.warning("Player setup file does not exist or cannot be read: " + file.getAbsolutePath());
      return new ArrayList<>(); // Return empty list, as if no setup exists
    }

    try (Reader reader = new FileReader(file)) {
      List<Player> loadedPlayers = PlayerCsvReaderWriter.readAll(reader);
      Logger.info("Successfully loaded " + loadedPlayers.size() + " players from " + file.getAbsolutePath());

      return loadedPlayers.stream()
          .map(player -> {
            Logger.debug("Mapping loaded player '" + player.getName() + "' to PlayerSetupData.");
            return new PlayerSetupData(player.getName(), player.getPieceIdentifier());
          })
          .collect(Collectors.toList());
    } catch (IOException e) {
      Logger.error("IOException occurred while loading player setup from " + file.getAbsolutePath(), e);
      throw e;
    } catch (InvalidParameterException e) {
      Logger.error("InvalidParameterException (malformed CSV) while loading player setup from " + file.getAbsolutePath(), e);
      throw e;
    }
  }
}