package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScene {
  private final GameController controller;
  private final BoardView boardView;
  private final VBox sidePanel;
  private final Map<Player, Label> playerLabels = new HashMap<>();
  private final Scene scene;

  public GameScene(Stage stage, BoardGame game, Runnable onNewGame, Runnable onHome) {
    controller = new GameController(game);
    boardView = new BoardView(game);

    // Toolbar with New Game and Home
    Button newGameBtn = new Button("New Game");
    newGameBtn.setOnAction(e -> onNewGame.run());
    Button homeBtn = new Button("Home");
    homeBtn.setOnAction(e -> onHome.run());
    ToolBar toolbar = new ToolBar(newGameBtn, homeBtn);

    // Side panel
    sidePanel = new VBox(15);
    sidePanel.setPadding(new Insets(10));
    sidePanel.setAlignment(Pos.TOP_CENTER);
    Label status = new Label("Game ready. Click Roll.");
    status.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    Button roll = new Button("Roll Dice");
    roll.setOnAction(e -> controller.playOneRound());
    sidePanel.getChildren().addAll(status, roll);

    // Player info
    Label playersHeader = new Label("Players:");
    playersHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    VBox playersBox = new VBox(8);
    playersBox.setPadding(new Insets(5, 0, 0, 0));
    for (Player p : game.getPlayers()) {
      Label lbl = new Label(p.getName() + ": Tile " + p.getCurrentTile().getId());
      playerLabels.put(p, lbl);
      playersBox.getChildren().add(lbl);
    }
    sidePanel.getChildren().addAll(playersHeader, playersBox);

    // Main layout
    BorderPane root = new BorderPane();
    root.setTop(toolbar);
    root.setCenter(boardView);
    root.setRight(sidePanel);
    root.setBottom(createFooter());
    root.setPadding(new Insets(10));

    scene = new Scene(root, 900, 700);
    stage.setScene(scene);
    stage.setTitle("Board Game");

    // Observer wiring
    controller.addListener(new GameController.GameListener() {
      @Override
      public void onGameStart(List<Player> players) {
        status.setText(players.get(0).getName() + " starts!");
        roll.setDisable(false);
        updatePlayerLabels();
      }

      @Override
      public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
        boardView.refresh();
        status.setText("Rolls: " + rolls);
        updatePlayerLabels();
      }

      @Override
      public void onGameOver(Player winner) {
        status.setText("Game over! " + winner.getName() + " wins!");
        roll.setDisable(true);
        updatePlayerLabels();
      }
    });
  }

  private HBox createFooter() {
    HBox footer = new HBox();
    footer.setPadding(new Insets(8));
    footer.setAlignment(Pos.CENTER);
    Label footerLabel = new Label("Thank you for playing!");
    footerLabel.setFont(Font.font(12));
    footer.getChildren().add(footerLabel);
    return footer;
  }

  private void updatePlayerLabels() {
    for (Map.Entry<Player, Label> entry : playerLabels.entrySet()) {
      Player p = entry.getKey();
      Label lbl = entry.getValue();
      lbl.setText(p.getName() + ": Tile " + p.getCurrentTile().getId());
    }
  }

  public void start() {
    controller.startGame();
  }

  public Scene getScene() {
    return scene;
  }
}
