package edu.ntnu.idi.bidata.ui;

import java.util.Objects;
import javafx.scene.Scene;

/**
 * Centralized UI styling (CSS) loader.
 */
public final class UiStyles {
  public static void apply(Scene scene) {
    scene.getStylesheets().add(
        Objects.requireNonNull(UiStyles.class.getResource("/css/styles.css")).toExternalForm());
  }
}