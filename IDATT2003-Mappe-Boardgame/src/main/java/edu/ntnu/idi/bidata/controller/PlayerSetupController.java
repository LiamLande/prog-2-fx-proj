package edu.ntnu.idi.bidata.controller; // Or controller.setup

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.file.PlayerCsvReaderWriter;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.ui.PlayerSetupData;
import edu.ntnu.idi.bidata.util.Logger; // Import the Logger

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerSetupController {

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