package edu.ntnu.idi.bidata.controller; // Or controller.setup

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.file.PlayerCsvReaderWriter;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.ui.PlayerSetupData; // Assuming this DTO exists

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

  /**
   * Handles the request to save player setup data.
   *
   * @param playerInputs The list of player setup data collected from the UI.
   * @param file The file to save the data to.
   * @return True if saving was successful, false otherwise.
   * @throws IOException If an I/O error occurs during writing.
   * @throws InvalidParameterException If player data is invalid during Player object creation.
   */
  public boolean savePlayerSetup(List<PlayerSetupData> playerInputs, File file) throws IOException, InvalidParameterException {
    if (playerInputs == null || playerInputs.isEmpty()) {
      // This validation could also be in the UI before calling,
      // but good to have defense here too.
      return false; // Or throw an IllegalArgumentException
    }
    if (file == null) {
      throw new IllegalArgumentException("File cannot be null for saving.");
    }

    List<Player> playerList = new ArrayList<>();
    // Tile ID for saving from setup is typically a placeholder (e.g., 0)
    // as actual tile assignment happens during game initialization.
    Tile placeholderStartTile = new Tile(0); // Assuming Tile(0) is valid

    for (PlayerSetupData input : playerInputs) {
      // Player constructor might throw InvalidParameterException
      playerList.add(new Player(input.name(), placeholderStartTile, input.pieceIdentifier()));
    }

    try (Writer writer = new FileWriter(file)) {
      PlayerCsvReaderWriter.writeAll(writer, playerList);
      return true;
    }
    // IOException is declared to be thrown
    // InvalidParameterException from PlayerCsvReaderWriter.writeAll (if players list is null, which we check)
  }

  /**
   * Handles the request to load player setup data.
   *
   * @param file The file to load the data from.
   * @return A list of PlayerSetupData to populate the UI, or an empty list if no valid data.
   * @throws IOException If an I/O error occurs during reading.
   * @throws InvalidParameterException If the CSV data is malformed as per PlayerCsvReaderWriter.
   */
  public List<PlayerSetupData> loadPlayerSetup(File file) throws IOException, InvalidParameterException {
    if (file == null) {
      throw new IllegalArgumentException("File cannot be null for loading.");
    }

    try (Reader reader = new FileReader(file)) {
      List<Player> loadedPlayers = PlayerCsvReaderWriter.readAll(reader); // Can throw IOException or InvalidParameterException

      // Convert List<Player> to List<PlayerSetupData> for the UI
      return loadedPlayers.stream()
          .map(player -> new PlayerSetupData(player.getName(), player.getPieceIdentifier()))
          .collect(Collectors.toList());
    }
  }
}