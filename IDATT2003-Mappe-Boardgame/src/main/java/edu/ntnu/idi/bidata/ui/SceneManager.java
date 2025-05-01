package edu.ntnu.idi.bidata.ui;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Centralized manager for JavaFX scenes, handling loading, caching, and transitions.
 */
public class SceneManager {
  private static SceneManager instance;
  private Stage primaryStage;
  private final Map<String, Supplier<Scene>> loaders = new HashMap<>();
  private final Map<String, Scene> cache = new HashMap<>();
  private String currentKey;

  private SceneManager() {}

  /**
   * Get the singleton instance.
   */
  public static SceneManager getInstance() {
    if (instance == null) {
      instance = new SceneManager();
    }
    return instance;
  }

  /**
   * Initialize with primary stage. Must be called once in MainApp.start().
   */
  public void initialize(Stage stage) {
    this.primaryStage = stage;
  }

  /**
   * Register a scene with a key and a loader supplier that provides a Scene.
   * @param key    Unique identifier for the scene
   * @param loader Supplier that builds the Scene
   */
  public void register(String key, Supplier<Scene> loader) {
    loaders.put(key, loader);
  }

  /**
   * Remove a scene from the cache so next show(key) creates a fresh one.
   */
  public void clear(String key) {
    cache.remove(key);
    if (key.equals(currentKey)) {
      currentKey = null;
    }
  }

  /**
   * Show a registered scene with default fade transition.
   * @param key Scene identifier
   */
  public void show(String key) {
    show(key, true);
  }

  /**
   * Show a registered scene, optionally with fade-in transition.
   * @param key           Scene identifier
   * @param useTransition Whether to apply a fade transition
   */
  public void show(String key, boolean useTransition) {
    if (primaryStage == null) {
      throw new IllegalStateException("SceneManager not initialized. Call initialize() first.");
    }
    Supplier<Scene> loader = loaders.get(key);
    if (loader == null) {
      throw new IllegalArgumentException("No scene registered with key: " + key);
    }

    // Retrieve or create the scene
    Scene scene = cache.computeIfAbsent(key, k -> loader.get());
    Parent root = scene.getRoot();

    // Notify old scene if present in cache
    if (currentKey != null) {
      Scene oldScene = cache.get(currentKey);
      if (oldScene != null) {
        Parent oldRoot = oldScene.getRoot();
        if (oldRoot instanceof ControlledScene) {
          ((ControlledScene) oldRoot).onHide();
        }
      }
    }

    // Notify new scene
    if (root instanceof ControlledScene) {
      ((ControlledScene) root).onShow();
    }

    // Apply transition
    if (useTransition) {
      root.setOpacity(0);
      FadeTransition ft = new FadeTransition(Duration.millis(400), root);
      ft.setFromValue(0);
      ft.setToValue(1);
      ft.play();
    }

    // Finally, set the scene on the stage
    primaryStage.setScene(scene);
    currentKey = key;
  }

  /**
   * Interface for scenes that want lifecycle callbacks.
   */
  public interface ControlledScene {
    /** Called when the scene is shown via SceneManager. */
    default void onShow() {}
    /** Called when the scene is hidden via SceneManager. */
    default void onHide() {}
  }
}
