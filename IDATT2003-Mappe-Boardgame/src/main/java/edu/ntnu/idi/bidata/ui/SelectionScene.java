// File: SelectionScene.java (cross-fade without white flash)
package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.app.GameVariant;
import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Selection screen using a composite image with smooth cross-fade between highlight overlays,
 * avoiding a white flash by layering views instead of swapping the single view.
 */
public class SelectionScene {
  private final Scene scene;
  private ImageView slOverlay;
  private ImageView monoOverlay;

  public SelectionScene(Stage stage, Consumer<GameVariant> onSelect) {
    // Base image
    Image base = loadImage("images/selection_default.png");
    Image slHi = loadImage("images/selection_highlight_sl.png");
    Image monoHi = loadImage("images/selection_highlight_mono.png");

    ImageView baseView = new ImageView(base);
    baseView.setPreserveRatio(true);
    baseView.setSmooth(true);
    baseView.setCache(true);
    double height = base.getHeight();
    baseView.setFitHeight(height);

    // Highlight overlays
    slOverlay = new ImageView(slHi);
    monoOverlay = new ImageView(monoHi);
    for (ImageView iv : new ImageView[]{slOverlay, monoOverlay}) {
      iv.setPreserveRatio(true);
      iv.setSmooth(true);
      iv.setCache(true);
      iv.setFitHeight(height);
      iv.setOpacity(0.0);
    }
    // Stack: base at bottom, overlays on top
    StackPane root = new StackPane(baseView, slOverlay, monoOverlay);

    scene = new Scene(root, base.getWidth(), height);

    // Hover logic: fade respective overlay in/out
    scene.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
      double x = e.getX();
      double w = scene.getWidth();
      if (x < w/2) {
        fadeOverlay(slOverlay, 1.0);
        fadeOverlay(monoOverlay, 0.0);
      } else {
        fadeOverlay(slOverlay, 0.0);
        fadeOverlay(monoOverlay, 1.0);
      }
    });
    scene.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
      fadeOverlay(slOverlay, 0.0);
      fadeOverlay(monoOverlay, 0.0);
    });

    // Click handlers
    scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
      double x = e.getX();
      double w = scene.getWidth();
      if (x < w/2) onSelect.accept(GameVariant.SNAKES_LADDERS);
      else onSelect.accept(GameVariant.MINI_MONOPOLY);
    });

    stage.setScene(scene);
    stage.setTitle("Select Game Variant");
  }

  /**
   * Fades an overlay ImageView to target opacity smoothly.
   */
  private void fadeOverlay(ImageView overlay, double targetOpacity) {
    FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
    ft.setToValue(targetOpacity);
    ft.play();
  }

  /**
   * Loads an image from the classpath, throwing if missing.
   */
  private Image loadImage(String path) {
    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
    if (is == null) throw new RuntimeException("Image not found: " + path);
    return new Image(is);
  }

  public Scene getScene() {
    return scene;
  }
}