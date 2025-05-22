package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.util.Logger;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents UI data for a player piece, including its identifier and image path.
 * This class is responsible for loading and providing the piece's image at different sizes.
 * It also provides methods for comparison and string representation.
 */
public class PieceUIData {
  private final String identifier;
  private final String imagePath;
  private Image image; // Cached image

  // Standard size for piece images on the board/UI
  private static final double DEFAULT_IMAGE_SIZE = 30; // Example: For SnakeLadderBoardView
  /**
   * Standard size for piece images when displayed in a ComboBox.
   */
  public static final double COMBOBOX_IMAGE_SIZE = 24; // For ComboBox display

  /**
   * Constructs a new PieceUIData object.
   *
   * @param identifier A unique string identifier for the piece (e.g., "car", "hat").
   * @param imagePath The path to the image resource for this piece.
   */
  public PieceUIData(String identifier, String imagePath) {
    this.identifier = identifier;
    this.imagePath = imagePath;
  }

  /**
   * Gets the identifier string for this piece.
   *
   * @return The identifier of the piece.
   */
  public String getIdentifier() {
    return identifier;
  }


  /**
   * Gets the image for the piece, loading it if necessary, scaled to a specific size.
   * @param requestedSize The desired size (width and height) for the image.
   * @return The Image object, or null if an error occurs during loading.
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

  /**
   * Gets the image for the piece scaled specifically for display in a ComboBox.
   * This is a convenience method that calls {@link #getImage(double)} with {@link #COMBOBOX_IMAGE_SIZE}.
   *
   * @return The Image object scaled for ComboBox display, or null if an error occurs.
   */
  public Image getComboBoxImage() {
    return getImage(COMBOBOX_IMAGE_SIZE);
  }


  /**
   * Returns a string representation of the piece, typically its identifier capitalized.
   * Used for display purposes, for example, in a ComboBox if images are not shown.
   *
   * @return The capitalized identifier, or an empty string if the identifier is null or empty.
   */
  @Override
  public String toString() {
    // Capitalize for display in ComboBox if text is shown
    if (identifier == null || identifier.isEmpty()) return "";
    return identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
  }

  /**
   * Compares this PieceUIData object with another object for equality.
   * Two PieceUIData objects are considered equal if their identifiers are equal.
   *
   * @param o The object to compare with.
   * @return True if the objects are equal, false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PieceUIData that = (PieceUIData) o;
    return Objects.equals(identifier, that.identifier);
  }

  /**
   * Computes the hash code for this PieceUIData object.
   * The hash code is based on the piece's identifier.
   *
   * @return The hash code value for this object.
   */
  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }
}