package edu.ntnu.idi.bidata.ui;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents UI data for a player piece, including its identifier and image.
 */
public class PieceUIData {
  private final String identifier;
  private final String imagePath;
  private Image image; // Lazily loaded image

  // Standard size for piece images on the board/UI
  private static final double DEFAULT_IMAGE_SIZE = 30; // For BoardView
  static final double COMBOBOX_IMAGE_SIZE = 24; // For ComboBox display

  public PieceUIData(String identifier, String imagePath) {
    this.identifier = identifier;
    this.imagePath = imagePath;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getImagePath() {
    return imagePath;
  }

  /**
   * Gets the image for the piece, loading it if necessary.
   * This version is for general use, e.g., on the game board.
   * @return The Image object.
   */
  public Image getImage() {
    return getImage(DEFAULT_IMAGE_SIZE);
  }

  /**
   * Gets the image for the piece, loading it if necessary, scaled to a specific size.
   * @param requestedSize The desired size (width and height) for the image.
   * @return The Image object.
   */
  public Image getImage(double requestedSize) {
    // Simple caching: if image is loaded and size matches, return it.
    // For more sophisticated caching with multiple sizes, a Map<Double, Image> could be used.
    // For now, let's assume the first loaded size is predominantly used or re-load for different specific sizes.
    if (this.image != null && this.image.getWidth() == requestedSize && this.image.getHeight() == requestedSize) {
      return this.image;
    }
    try {
      InputStream is = PieceUIData.class.getResourceAsStream(this.imagePath);
      if (is == null && !this.imagePath.startsWith("/")) { // Try adding leading slash if not present
        is = PieceUIData.class.getResourceAsStream("/" + this.imagePath);
      }
      Objects.requireNonNull(is, "Cannot load image resource: " + this.imagePath);
      this.image = new Image(is, requestedSize, requestedSize, true, true); // Preserve ratio, smooth scaling
    } catch (Exception e) {
      System.err.println("Error loading piece image: " + this.imagePath + " for size " + requestedSize);
      e.printStackTrace();
      // Fallback: return a null or a default placeholder image if you have one
      // To avoid NPEs, ensure callers handle null or have a default.
      // For now, returning null if load fails.
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