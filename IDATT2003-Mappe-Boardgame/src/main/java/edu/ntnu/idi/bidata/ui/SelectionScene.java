package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.app.GameVariant;
import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
/**
 * Selection screen using overlays; no longer sets Stage directly.
 */
public class SelectionScene implements SceneManager.ControlledScene {
  private final Scene scene;
  private final ImageView slOverlay;
  private final ImageView monoOverlay;

  public SelectionScene(Stage stage, Consumer<GameVariant> onSelect) {
    Image base    = loadImage("images/selection_default.png");
    Image slHi    = loadImage("images/selection_highlight_sl.png");
    Image monoHi  = loadImage("images/selection_highlight_mono.png");

    // Make images fit the entire scene
    ImageView baseView = new ImageView(base);
    baseView.setPreserveRatio(true);
    baseView.setSmooth(true);

    slOverlay = new ImageView(slHi);
    monoOverlay = new ImageView(monoHi);

    // Configure all image views to scale with parent
    for (ImageView iv : new ImageView[]{baseView, slOverlay, monoOverlay}) {
      iv.setPreserveRatio(true);
      iv.setSmooth(true);
      iv.fitWidthProperty().bind(stage.widthProperty());
      iv.fitHeightProperty().bind(stage.heightProperty());
    }

    slOverlay.setOpacity(0);
    monoOverlay.setOpacity(0);

    // Create text elements with the same styling as before
    Text titleText = new Text("SELECT A GAME");
    titleText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
    titleText.setFill(Color.WHITE);
    titleText.setStroke(Color.BLACK);
    titleText.setStrokeWidth(2);

    Text snakesText = new Text("Snakes & Ladders");
    snakesText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    snakesText.setFill(Color.WHITE);
    snakesText.setStroke(Color.BLACK);
    snakesText.setStrokeWidth(1.5);

    Text monopolyText = new Text("Mini Monopoly");
    monopolyText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    monopolyText.setFill(Color.WHITE);
    monopolyText.setStroke(Color.BLACK);
    monopolyText.setStrokeWidth(1.5);

    // Use AnchorPane for proper positioning
    javafx.scene.layout.AnchorPane anchorPane = new javafx.scene.layout.AnchorPane();

    // Add title to anchor pane with top center positioning
    anchorPane.getChildren().add(titleText);
    javafx.scene.layout.AnchorPane.setTopAnchor(titleText, 30.0);
    javafx.scene.layout.AnchorPane.setLeftAnchor(titleText, 0.0);
    javafx.scene.layout.AnchorPane.setRightAnchor(titleText, 0.0);
    titleText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

    // Add game names to anchor pane with bottom corner positioning
    anchorPane.getChildren().add(snakesText);
    javafx.scene.layout.AnchorPane.setBottomAnchor(snakesText, 30.0);
    javafx.scene.layout.AnchorPane.setLeftAnchor(snakesText, 50.0);

    anchorPane.getChildren().add(monopolyText);
    javafx.scene.layout.AnchorPane.setBottomAnchor(monopolyText, 30.0);
    javafx.scene.layout.AnchorPane.setRightAnchor(monopolyText, 50.0);

    // Create the main stack pane and make it fill the window
    StackPane imagePane = new StackPane(baseView, slOverlay, monoOverlay);
    StackPane root = new StackPane(imagePane, anchorPane);

    scene = new Scene(root);

    // Make scene resize with window
    stage.widthProperty().addListener((obs, oldVal, newVal) -> {
      root.setPrefWidth(newVal.doubleValue());
    });

    stage.heightProperty().addListener((obs, oldVal, newVal) -> {
      root.setPrefHeight(newVal.doubleValue());
    });

    // Keep the same event handlers
    scene.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
      double x = e.getX(), w = scene.getWidth();
      if (x < w/2) { fadeOverlay(slOverlay,1); fadeOverlay(monoOverlay,0); }
      else         { fadeOverlay(slOverlay,0); fadeOverlay(monoOverlay,1); }
    });

    scene.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
      fadeOverlay(slOverlay,0); fadeOverlay(monoOverlay,0);
    });

    scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
      double x = e.getX(), w = scene.getWidth();
      if (x < w/2) onSelect.accept(GameVariant.SNAKES_LADDERS);
      else         onSelect.accept(GameVariant.MINI_MONOPOLY);
    });
  }

  public Scene getScene()  { return scene;       }

  private void fadeOverlay(ImageView overlay, double target) {
    FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
    ft.setToValue(target);
    ft.play();
  }

  private Image loadImage(String path) {
    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
    if (is == null) throw new RuntimeException("Image not found: " + path);
    return new Image(is);
  }

  @Override public void onShow() {}
  @Override public void onHide() {}
}