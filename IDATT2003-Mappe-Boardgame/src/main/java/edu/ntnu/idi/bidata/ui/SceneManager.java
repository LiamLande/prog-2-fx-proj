package edu.ntnu.idi.bidata.ui;

import javafx.animation.FadeTransition;
// import javafx.scene.Parent; // No longer needed here for onShow/onHide
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Centralized manager for JavaFX scenes, handling loading, caching, and transitions
 * by managing ControlledScene instances.
 */
public class SceneManager {
  private static SceneManager instance;
  private Stage primaryStage;
  // MODIFIED: Store suppliers that create ControlledScene instances
  private final Map<String, Supplier<ControlledScene>> loaders = new HashMap<>();
  // MODIFIED: Cache the ControlledScene instances themselves
  private final Map<String, ControlledScene> cache = new HashMap<>();
  private String currentKey;
  private ControlledScene currentController; // Keep track of the current controller instance

  private SceneManager() {}

  public static SceneManager getInstance() {
    if (instance == null) {
      instance = new SceneManager();
    }
    return instance;
  }

  public void initialize(Stage stage) {
    if (stage == null) {
      throw new IllegalArgumentException("Primary Stage cannot be null.");
    }
    this.primaryStage = stage;
  }

  /**
   * MODIFIED: Register a scene controller with a key and a supplier that provides a ControlledScene instance.
   * @param key    Unique identifier for the scene/controller
   * @param loader Supplier that builds the ControlledScene instance
   */
  public void register(String key, Supplier<ControlledScene> loader) {
    if (key == null || key.isBlank() || loader == null) {
      throw new IllegalArgumentException("Key and loader must not be null or blank.");
    }
    loaders.put(key, loader);
  }

  public void clear(String key) {
    if (key == null) return;
    cache.remove(key);
    if (key.equals(currentKey)) {
      currentKey = null;
      currentController = null;
    }
  }

  public void show(String key) {
    show(key, true);
  }

  public void show(String key, boolean useTransition) {
    if (primaryStage == null) {
      throw new IllegalStateException("SceneManager not initialized. Call initialize() first.");
    }
    Objects.requireNonNull(key, "Scene key cannot be null.");

    Supplier<ControlledScene> loader = loaders.get(key);
    if (loader == null) {
      throw new IllegalArgumentException("No scene controller registered with key: " + key);
    }

    // MODIFIED: Retrieve or create the scene CONTROLLER instance
    ControlledScene nextController = cache.computeIfAbsent(key, k -> {
      ControlledScene newController = loader.get();
      if (newController == null || newController.getScene() == null) {
        throw new IllegalStateException(
            "Loader for key '" + k + "' returned null controller or controller with null scene.");
      }
      if (newController.getScene().getRoot() == null) {
        throw new IllegalStateException("Scene for key '" + k + "' (from " + newController.getClass().getSimpleName() + ") has a null root node.");
      }
      return newController;
    });

    Scene nextScene = nextController.getScene(); // Get Scene from controller

    // Notify old controller
    if (currentController != null && currentController != nextController) {
      currentController.onHide();
    }

    // Notify new controller
    nextController.onShow();

    // Apply transition to the scene's root
    if (useTransition) {
      nextScene.getRoot().setOpacity(0);
      FadeTransition ft = new FadeTransition(Duration.millis(400), nextScene.getRoot());
      ft.setFromValue(0);
      ft.setToValue(1.0);
      ft.setOnFinished(event -> nextScene.getRoot().setOpacity(1.0));
      ft.play();
    } else {
      nextScene.getRoot().setOpacity(1.0);
    }

    primaryStage.setScene(nextScene);
    currentKey = key;
    currentController = nextController;
  }

  /**
   * MODIFIED: Interface for scene controllers managed by SceneManager.
   */
  public interface ControlledScene {
    /**
     * Returns the JavaFX Scene associated with this controller.
     * @return The Scene object. Must not be null.
     */
    Scene getScene(); // ADDED this method

    default void onShow() {}
    default void onHide() {}
  }
}