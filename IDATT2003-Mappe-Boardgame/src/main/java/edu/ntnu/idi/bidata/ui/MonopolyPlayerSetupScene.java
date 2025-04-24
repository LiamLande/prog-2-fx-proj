package edu.ntnu.idi.bidata.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Monopoly-themed player setup screen with Wall Street celebration background,
 * styled inputs, start button, and home button.
 */
public class MonopolyPlayerSetupScene {
  private final Scene scene;

  public MonopolyPlayerSetupScene(Stage stage,
      Consumer<List<String>> onStart,
      Runnable onHome) {
    // --- Background ---
    Image bgImg = loadImage("images/monopoly_setup_bg.png");
    ImageView bgView = new ImageView(bgImg);
    bgView.setFitWidth(bgImg.getWidth());
    bgView.setFitHeight(bgImg.getHeight());
    bgView.setPreserveRatio(true);

    // --- Title ---
    Label title = new Label("ENTER YOUR INVESTORS' NAMES");
    title.getStyleClass().add("scene-title");

    // --- Player grid (2×2) ---
    GridPane grid = new GridPane();
    grid.setHgap(40);
    grid.setVgap(30);
    grid.setAlignment(Pos.CENTER);

    List<TextField> fields = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Label lbl = new Label("Player " + (i + 1));
      lbl.getStyleClass().add("player-label");

      TextField tf = new TextField();
      tf.setPromptText("Name...");
      tf.getStyleClass().add("player-input");
      tf.setFont(Font.font(16));
      tf.setPrefWidth(260);

      int row = (i < 2) ? 0 : 1;
      int col = i % 2;
      grid.add(lbl, col, row * 2);
      grid.add(tf, col, row * 2 + 1);
      fields.add(tf);
    }

    // --- Launch button ---
    Button startBtn = new Button("LAUNCH MARKET");
    startBtn.getStyleClass().add("start-button");
    startBtn.setOnAction(e -> {
      List<String> names = new ArrayList<>();
      for (TextField tf : fields) {
        String nm = tf.getText().trim();
        if (!nm.isEmpty()) names.add(nm);
      }
      if (names.size() < 2) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText("Please enter at least two investor names.");
        a.showAndWait();
      } else {
        onStart.accept(names);
      }
    });

    // --- Home button ---
    Image homeImg = loadImage("images/home_icon.png");
    ImageView homeView = new ImageView(homeImg);
    homeView.setFitWidth(40);
    homeView.setPreserveRatio(true);
    Button homeBtn = new Button();
    homeBtn.setGraphic(homeView);
    homeBtn.setBackground(Background.EMPTY);
    homeBtn.setOnAction(e -> onHome.run());

    // --- Layout composition ---
    VBox content = new VBox(30, title, grid, startBtn);
    content.setAlignment(Pos.CENTER);

    StackPane root = new StackPane(bgView, content, homeBtn);
    StackPane.setAlignment(homeBtn, Pos.TOP_LEFT);
    StackPane.setMargin(homeBtn, new Insets(20));

    scene = new Scene(root, bgImg.getWidth(), bgImg.getHeight());
    UiStyles.apply(scene);

    stage.setScene(scene);
    stage.setTitle("Mini Monopoly Setup");
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
