package edu.ntnu.idi.bidata.view;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import java.util.Arrays;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class GameView extends VBox {
  private final TextField playerNameInput;
  private final Button addPlayerButton;
  private final Button nextRoundButton;
  private final Label gameStatus;
  private final TableView<Player> playerTable;

  public GameView() {
    setSpacing(10);
    setAlignment(Pos.CENTER);

    playerNameInput = new TextField();
    playerNameInput.setPromptText("Enter player name");

    addPlayerButton = new Button("Add Player");
    nextRoundButton = new Button("Next Round");
    gameStatus = new Label("Welcome to Snakes and Ladders!");

    // Initialize TableView
    playerTable = new TableView<>();
    setupPlayerTable();

    getChildren().addAll(playerNameInput, addPlayerButton, playerTable, nextRoundButton, gameStatus);
  }

  private void setupPlayerTable() {
    // Column for player names
    TableColumn<Player, String> nameColumn = new TableColumn<>("Player");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setMinWidth(150);

    // Column for tile position
    TableColumn<Player, Integer> tileColumn = new TableColumn<>("Tile");
    tileColumn.setCellValueFactory(cellData -> {
      Player player = cellData.getValue(); // Get the Player object for this row
      Tile currentTile = player.getCurrentTile(); // Get the Tile object
      return new SimpleIntegerProperty(currentTile.getTileId()).asObject(); // Extract tileId
    });
    tileColumn.setMinWidth(100);

    playerTable.getColumns().addAll(Arrays.asList(nameColumn, tileColumn));
  }

  public TextField getPlayerNameInput() {
    return playerNameInput;
  }

  public Button getAddPlayerButton() {
    return addPlayerButton;
  }

  public Button getNextRoundButton() {
    return nextRoundButton;
  }

  public void updatePlayerList(List<Player> players) {
    playerTable.getItems().setAll(players); // Refresh table with updated tile and score

  }

  public void updateStatus(String message) {
    gameStatus.setText(message);
  }
}
