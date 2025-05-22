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
 * Centralized manager for JavaFX scenes. This singleton class handles the registration,
 * loading, caching, and display of scenes, along with transitions between them.
 * It uses {@link ControlledScene} instances to manage individual scenes and their lifecycles.
 */
public class SceneManager {
  private static SceneManager instance;
  private Stage primaryStage;
  private final Map<String, Supplier<ControlledScene>> loaders = new HashMap<>();
  private final Map<String, ControlledScene> cache = new HashMap<>();
  private String currentKey;
  private ControlledScene currentController;

  private SceneManager() {}

  /**
   * Gets the singleton instance of the SceneManager.
   *
   * @return The single instance of SceneManager.
   */
  public static SceneManager getInstance() {
    if (instance == null) {
      instance = new SceneManager();
    }
    return instance;
  }

  /**
   * Initializes the SceneManager with the primary stage of the application.
   * This method must be called before any scenes can be shown.
   *
   * @param stage The primary {@link Stage} for the application.
   * @throws IllegalArgumentException if the provided stage is null.
   */
  public void initialize(Stage stage) {
    if (stage == null) {
      throw new IllegalArgumentException("Primary Stage cannot be null.");
    }
    this.primaryStage = stage;
  }

  /**
   * Registers a scene supplier with a unique key. The supplier is responsible for creating
   * an instance of a {@link ControlledScene} when requested.
   *
   * @param key    A unique string identifier for the scene/controller.
   * @param loader A {@link Supplier} that constructs and returns the {@link ControlledScene} instance.
   * @throws IllegalArgumentException if the key or loader is null or the key is blank.
   */
  public void register(String key, Supplier<ControlledScene> loader) {
    if (key == null || key.isBlank() || loader == null) {
      throw new IllegalArgumentException("Key and loader must not be null or blank.");
    }
    loaders.put(key, loader);
  }

  /**
   * Clears a specific scene and its controller from the cache.
   * If the cleared scene is currently active, the current scene and controller references are nullified.
   *
   * @param key The key of the scene to clear from the cache. If null, the method does nothing.
   */
  public void clear(String key) {
    if (key == null) return;
    cache.remove(key);
    if (key.equals(currentKey)) {
      currentKey = null;
      currentController = null;
    }
  }

  /**
   * Shows the scene associated with the given key, using a default transition.
   * This is a convenience method for {@link #show(String, boolean)} with useTransition set to true.
   *
   * @param key The key of the scene to show.
   * @throws IllegalStateException if SceneManager is not initialized or if the scene cannot be loaded.
   * @throws IllegalArgumentException if no scene is registered with the given key.
   */
  public void show(String key) {
    show(key, true);
  }

  /**
   * Shows the scene associated with the given key, with an option to use a transition effect.
   * It loads the scene if not already cached, calls lifecycle methods on controllers, and sets the scene on the primary stage.
   *
   * @param key The key of the scene to show.
   * @param useTransition If true, a fade transition is applied; otherwise, the scene is shown immediately.
   * @throws IllegalStateException if SceneManager is not initialized or if the scene/controller cannot be properly loaded (e.g., null scene, null root).
   * @throws IllegalArgumentException if no scene is registered with the given key.
   */
  public void show(String key, boolean useTransition) {
    if (primaryStage == null) {
      throw new IllegalStateException("SceneManager not initialized. Call initialize() first.");
    }
    Objects.requireNonNull(key, "Scene key cannot be null.");

    Supplier<ControlledScene> loader = loaders.get(key);
    if (loader == null) {
      throw new IllegalArgumentException("No scene controller registered with key: " + key);
    }

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
   * Gets the controller of the currently displayed scene.
   *
   * @return The current {@link ControlledScene} instance, or null if no scene is active.
   */
  public Object getCurrentController() {
    return currentController;
  }

  /**
   * Interface for scene controllers managed by SceneManager.
   * Implementing classes must provide a {@link Scene} and can optionally override
   * {@code onShow()} and {@code onHide()} for lifecycle management.
   */
  public interface ControlledScene {
    /**
     * Returns the JavaFX Scene associated with this controller.
     * The scene must have a non-null root node.
     *
     * @return The {@link Scene} object. Must not be null.
     */
    Scene getScene();

    /**
     * Called when the scene is about to be shown.
     * Default implementation does nothing.
     */
    default void onShow() {}
    /**
     * Called when the scene is about to be hidden (another scene is shown).
     * Default implementation does nothing.
     */
    default void onHide() {}
  }
}