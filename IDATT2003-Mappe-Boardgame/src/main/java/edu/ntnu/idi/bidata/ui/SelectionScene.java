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

    ImageView baseView = new ImageView(base);
    baseView.setPreserveRatio(true);
    baseView.setSmooth(true);
    baseView.setCache(true);
    double height = base.getHeight();
    baseView.setFitHeight(height);

    slOverlay   = new ImageView(slHi);
    monoOverlay = new ImageView(monoHi);
    for (ImageView iv : new ImageView[]{slOverlay, monoOverlay}) {
      iv.setPreserveRatio(true);
      iv.setSmooth(true);
      iv.setCache(true);
      iv.setFitHeight(height);
      iv.setOpacity(0.0);
    }

    StackPane root = new StackPane(baseView, slOverlay, monoOverlay);
    scene = new Scene(root, base.getWidth(), height);

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
      else          onSelect.accept(GameVariant.MINI_MONOPOLY);
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