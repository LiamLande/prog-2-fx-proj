package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.app.GameVariant;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class SelectionScene implements SceneManager.ControlledScene {
  private final Scene scene;

  public SelectionScene(Stage stage, Consumer<GameVariant> onSelect) {
    Image baseImg = loadImage("/images/selection_default.png");
    Image slHiImg = loadImage("/images/selection_highlight_sl.png");
    Image monoHiImg = loadImage("/images/selection_highlight_mono.png");

    ImageView baseView = new ImageView(baseImg);
    ImageView slOverlay = new ImageView(slHiImg);
    ImageView monoOverlay = new ImageView(monoHiImg);

    for (ImageView iv : new ImageView[]{baseView, slOverlay, monoOverlay}) {
      iv.setSmooth(true);
      iv.fitWidthProperty().bind(stage.widthProperty());
      iv.fitHeightProperty().bind(stage.heightProperty());
      iv.setPreserveRatio(false);
    }

    slOverlay.setOpacity(0);
    monoOverlay.setOpacity(0);

    String gameFontFamily = "Trajan Pro";
    try {
      Font.font(gameFontFamily);
    } catch (Exception e) {
      gameFontFamily = "Arial";
      System.err.println("Warning: Font 'Trajan Pro' not found, falling back to 'Arial'.");
    }

    // Game Name Text Styling (common style)
    Font gameNameFont = Font.font(gameFontFamily, FontWeight.SEMI_BOLD, 28);
    Color gameNameFill = Color.WHITESMOKE;
    // Using simpler DropShadow constructor for game names
    DropShadow gameNameShadow = new DropShadow(3, 2, 2, Color.rgb(0,0,0,0.8));


    Text snakesText = new Text("Snakes & Ladders");
    snakesText.setFont(gameNameFont);
    snakesText.setFill(gameNameFill);
    snakesText.setEffect(gameNameShadow);


    Text monopolyText = new Text("Mini Monopoly");
    monopolyText.setFont(gameNameFont);
    monopolyText.setFill(gameNameFill);
    monopolyText.setEffect(gameNameShadow);

    AnchorPane textOverlayPane = new AnchorPane();

    // Snakes & Ladders Text Positioning
    textOverlayPane.getChildren().add(snakesText);
    AnchorPane.setBottomAnchor(snakesText, 60.0);
    AnchorPane.setLeftAnchor(snakesText, 70.0);

    // Monopoly Text Positioning
    textOverlayPane.getChildren().add(monopolyText);
    AnchorPane.setBottomAnchor(monopolyText, 60.0);
    AnchorPane.setRightAnchor(monopolyText, 70.0);

    StackPane imagePane = new StackPane(baseView, slOverlay, monoOverlay);
    StackPane root = new StackPane(imagePane, textOverlayPane);
    scene = new Scene(root);

    scene.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
      double x = e.getX();
      double w = scene.getWidth();
      if (w == 0) return;
      if (x < w / 2) {
        fadeOverlay(slOverlay, 1.0);
        fadeOverlay(monoOverlay, 0.0);
      } else {
        fadeOverlay(slOverlay, 0.0);
        fadeOverlay(monoOverlay, 1.0);
      }
    });

    scene.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> {
      fadeOverlay(slOverlay, 0.0);
      fadeOverlay(monoOverlay, 0.0);
    });

    scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
      double x = e.getX();
      double w = scene.getWidth();
      if (w == 0) return;
      if (x < w / 2) {
        onSelect.accept(GameVariant.SNAKES_LADDERS);
      } else {
        onSelect.accept(GameVariant.MINI_MONOPOLY);
      }
    });
  }

  @Override
  public Scene getScene() {
    return this.scene;
  }

  private void fadeOverlay(ImageView overlay, double targetOpacity) {
    if (overlay == null) return;
    FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
    ft.setToValue(targetOpacity);
    ft.play();
  }

  private Image loadImage(String resourcePath) {
    String correctedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
    InputStream is = getClass().getResourceAsStream(correctedPath);
    if (is == null) {
      is = getClass().getClassLoader().getResourceAsStream(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
    }
    Objects.requireNonNull(is, "Cannot load image resource from path: " + resourcePath +
        ". Corrected attempted path: " + correctedPath +
        ". Ensure the image is in '/resources" + correctedPath + "' and the path is correct from classpath root.");
    return new Image(is);
  }
}