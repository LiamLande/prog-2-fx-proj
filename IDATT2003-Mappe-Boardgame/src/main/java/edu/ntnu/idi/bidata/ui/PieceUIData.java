package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.util.Logger;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents UI data for a player piece, including its identifier and image.
 */
public class PieceUIData {
  private final String identifier;
  private final String imagePath;
  private Image image;

  // Standard size for piece images on the board/UI
  private static final double DEFAULT_IMAGE_SIZE = 30; // For SnakeLadderBoardView
  public static final double COMBOBOX_IMAGE_SIZE = 24; // For ComboBox display

  public PieceUIData(String identifier, String imagePath) {
    this.identifier = identifier;
    this.imagePath = imagePath;
  }

  public String getIdentifier() {
    return identifier;
  }


  /**
   * Gets the image for the piece, loading it if necessary, scaled to a specific size.
   * @param requestedSize The desired size (width and height) for the image.
   * @return The Image object.
   */
  public Image getImage(double requestedSize) {
    if (this.image != null && this.image.getWidth() == requestedSize && this.image.getHeight() == requestedSize) {
      return this.image;
    }
    try {
      InputStream is = PieceUIData.class.getResourceAsStream(this.imagePath);
      if (is == null && !this.imagePath.startsWith("/")) {
        is = PieceUIData.class.getResourceAsStream("/" + this.imagePath);
      }
      Objects.requireNonNull(is, "Cannot load image resource: " + this.imagePath);
      this.image = new Image(is, requestedSize, requestedSize, true, true); // Preserve ratio, smooth scaling
    } catch (Exception e) {
      Logger.error("Error loading piece image: " + this.imagePath + " for size " + requestedSize, e);
      return null;
    }
    return this.image;
  }

  public Image getComboBoxImage() {
    return getImage(COMBOBOX_IMAGE_SIZE);
  }


  @Override
  public String toString() {
    // Capitalize for display in ComboBox if text is shown
    if (identifier == null || identifier.isEmpty()) return "";
    return identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PieceUIData that = (PieceUIData) o;
    return Objects.equals(identifier, that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }
}