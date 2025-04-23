package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.app.GameVariant;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Initial menu scene for selecting the game variant.
 */
public class SelectionScene {
  private final Scene scene;

  public SelectionScene(Stage stage, Consumer<GameVariant> onSelect) {
    Button snakesBtn = new Button("Snakes & Ladders");
    snakesBtn.setPrefWidth(200);
    snakesBtn.setOnAction(e -> onSelect.accept(GameVariant.SNAKES_LADDERS));

    Button monopolyBtn = new Button("Mini Monopoly");
    monopolyBtn.setPrefWidth(200);
    monopolyBtn.setOnAction(e -> onSelect.accept(GameVariant.MINI_MONOPOLY));

    VBox root = new VBox(20, snakesBtn, monopolyBtn);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(40));

    this.scene = new Scene(root, 400, 300);
    stage.setScene(scene);
    stage.setTitle("Select Game Variant");
  }

  public Scene getScene() {
    return scene;
  }
}