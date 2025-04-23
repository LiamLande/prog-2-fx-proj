package edu.ntnu.idi.bidata.app;

import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.ui.GameScene;
import edu.ntnu.idi.bidata.ui.SelectionScene;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Main JavaFX application with variant selection and player setup.
 */
public class MainApp extends Application {
  private Stage primaryStage;
  private GameVariant selectedVariant;

  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;
    showSelection();
    stage.show();
  }

  private void showSelection() {
    SelectionScene selection = new SelectionScene(primaryStage, variant -> {
      this.selectedVariant = variant;
      showPlayerSetup();
    });
    primaryStage.setScene(selection.getScene());
  }

  private void showPlayerSetup() {
    Label title = new Label("Enter Player Names (2-4)");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));
    grid.setAlignment(Pos.CENTER);

    List<TextField> fields = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Label lbl = new Label("Player " + (i + 1) + ":");
      TextField tf = new TextField();
      tf.setPromptText("Name");
      grid.add(lbl, 0, i);
      grid.add(tf, 1, i);
      fields.add(tf);
    }

    Button startBtn = new Button("Start Game");
    startBtn.setDefaultButton(true);
    startBtn.setOnAction(e -> {
      List<String> names = new ArrayList<>();
      for (TextField tf : fields) {
        String name = tf.getText().trim();
        if (!name.isEmpty()) names.add(name);
      }
      if (names.size() < 2) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Players");
        alert.setHeaderText(null);
        alert.setContentText("Please enter at least two player names.");
        alert.showAndWait();
      } else {
        startGame(names);
      }
    });

    VBox root = new VBox(15, title, grid, startBtn);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(20));

    Scene scene = new Scene(root, 400, 350);
    primaryStage.setScene(scene);
    primaryStage.setTitle(selectedVariant == GameVariant.SNAKES_LADDERS ?
        "Snakes & Ladders - Setup" : "Mini Monopoly - Setup");
  }

  private void startGame(List<String> playerNames) {
    // TODO: branch based on selectedVariant for mini monopoly support
    BoardGame game = GameFactory.createGame(playerNames);

    GameScene gameScene = new GameScene(primaryStage, game);
    primaryStage.setScene(gameScene.getScene());
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(650);
    gameScene.start();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
