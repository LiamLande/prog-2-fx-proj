// File: PlayerSetupScene.java
package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.app.GameVariant;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Egyptian-themed player setup screen with styled inputs, start button, and home button.
 */
public class PlayerSetupScene {
  private final Scene scene;

  public PlayerSetupScene(Stage stage,
      Consumer<List<String>> onStart,
      Runnable onHome) {
    // Load background image
    Image bgImg = loadImage("images/player_setup_bg.png");
    ImageView bgView = new ImageView(bgImg);
    bgView.setFitWidth(bgImg.getWidth());
    bgView.setFitHeight(bgImg.getHeight());
    bgView.setPreserveRatio(true);

    // Title
    Label title = new Label("ENTER YOUR LEGENDS' NAMES");
    title.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 36));
    title.setTextFill(Color.web("#d4af37")); // gold
    title.setEffect(new DropShadow(3, Color.BLACK));

    // Input grid
    GridPane grid = new GridPane();
    grid.setHgap(40);
    grid.setVgap(30);
    grid.setAlignment(Pos.CENTER);

    List<TextField> fields = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Label lbl = new Label("Player " + (i+1));
      lbl.setFont(Font.font("Trajan Pro", FontWeight.SEMI_BOLD, 20));
      lbl.setTextFill(Color.web("#f0e68c")); // khaki

      TextField tf = new TextField();
      tf.setPromptText("Name...");
      tf.setFont(Font.font(18));
      tf.setPrefWidth(280);
      tf.setBackground(new Background(new BackgroundFill(Color.color(0,0,0,0.4), new CornerRadii(5), Insets.EMPTY)));
      tf.setStyle("-fx-prompt-text-fill: gray; -fx-text-fill: cyan; -fx-border-color: cyan; -fx-border-radius:5; -fx-background-radius:5;");
      tf.setEffect(new DropShadow(10, Color.CYAN));

      int row = i < 2 ? 0 : 1;
      int col = i % 2;
      grid.add(lbl, col, row*2);
      grid.add(tf, col, row*2+1);
      fields.add(tf);
    }

    // Start button
    Button startBtn = new Button("START ADVENTURE");
    startBtn.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 24));
    startBtn.setTextFill(Color.WHITE);
    startBtn.setPadding(new Insets(10,40,10,40));
    startBtn.setBackground(new Background(new BackgroundFill(
        Color.web("#b8860b"), new CornerRadii(8), Insets.EMPTY)));
    startBtn.setEffect(new DropShadow(5, Color.BLACK));
    startBtn.setOnAction(e -> {
      List<String> names = new ArrayList<>();
      for (TextField tf: fields) {
        String nm = tf.getText().trim();
        if (!nm.isEmpty()) names.add(nm);
      }
      if (names.size() < 2) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText("Enter at least two legendary names.");
        a.showAndWait();
      } else {
        onStart.accept(names);
      }
    });

    // Home button (icon)
    Image homeImg = loadImage("images/home_icon.png");
    ImageView homeView = new ImageView(homeImg);
    homeView.setFitWidth(40);
    homeView.setPreserveRatio(true);
    Button homeBtn = new Button();
    homeBtn.setGraphic(homeView);
    homeBtn.setBackground(Background.EMPTY);
    homeBtn.setOnAction(e -> onHome.run());

    // Layout
    VBox content = new VBox(30, title, grid, startBtn);
    content.setAlignment(Pos.CENTER);

    StackPane root = new StackPane(bgView, content, homeBtn);
    StackPane.setAlignment(homeBtn, Pos.TOP_LEFT);
    StackPane.setMargin(homeBtn, new Insets(20));

    scene = new Scene(root, bgImg.getWidth(), bgImg.getHeight());
    stage.setScene(scene);
    stage.setTitle("Player Setup");
  }

  private Image loadImage(String path) {
    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
    if (is == null) throw new RuntimeException("Resource not found: " + path);
    return new Image(is);
  }

  public Scene getScene() {
    return scene;
  }
}
