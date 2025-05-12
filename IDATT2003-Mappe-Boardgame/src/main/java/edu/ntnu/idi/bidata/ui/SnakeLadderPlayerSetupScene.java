package edu.ntnu.idi.bidata.ui;

// --- Necessary Imports ---
import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.file.PlayerCsvReaderWriter;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader; // Added for loading
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader; // Added for loading
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
// java.lang.Runnable is implicitly available

/**
 * Egypt/Jungle-themed player setup; allows theme switching, saving, and loading player names.
 * Passes selected theme to the onStart callback.
 */
public class SnakeLadderPlayerSetupScene implements SceneManager.ControlledScene {

  public enum Theme {
    EGYPT, JUNGLE
  }

  private final Scene scene;
  private final Stage stage;
  private final ImageView bgView;
  private final Button themeButton;
  private final Button saveButton;
  private final Button loadButton; // Added Load Button
  private final List<TextField> fields;

  private Theme currentTheme = Theme.EGYPT;

  private static final String EGYPT_BG_PATH = "images/player_setup_bg.png";
  private static final String JUNGLE_BG_PATH = "images/jungle_setup_bg.png";

  public SnakeLadderPlayerSetupScene(Stage stage,
      BiConsumer<List<String>, Theme> onStart,
      Runnable onHome) { // java.lang.Runnable

    this.stage = stage;

    Image bgImg = loadImage(EGYPT_BG_PATH);
    bgView = new ImageView(bgImg);
    bgView.setFitWidth(bgImg.getWidth());
    bgView.setFitHeight(bgImg.getHeight());
    bgView.setPreserveRatio(true);

    Label title = new Label("ENTER YOUR LEGENDS' NAMES");
    title.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 36));
    title.setTextFill(Color.web("#d4af37"));
    title.setEffect(new DropShadow(3, Color.BLACK));

    GridPane grid = new GridPane();
    grid.setHgap(40);
    grid.setVgap(30);
    grid.setAlignment(Pos.CENTER);

    fields = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Label lbl = new Label("Player " + (i + 1));
      lbl.setFont(Font.font("Trajan Pro", FontWeight.SEMI_BOLD, 20));
      lbl.setTextFill(Color.web("#f0e68c"));

      TextField tf = new TextField();
      tf.setPromptText("Name...");
      tf.setFont(Font.font(18));
      tf.setPrefWidth(280);
      tf.setBackground(new Background(new BackgroundFill(
          Color.color(0, 0, 0, 0.4), new CornerRadii(5), Insets.EMPTY)));
      tf.setStyle(
          "-fx-prompt-text-fill: gray; -fx-text-fill: cyan; -fx-border-color: cyan; -fx-border-radius:5; -fx-background-radius:5;");
      tf.setEffect(new DropShadow(10, Color.CYAN));

      int row = i < 2 ? 0 : 1;
      int col = i % 2;
      grid.add(lbl, col, row * 2);
      grid.add(tf, col, row * 2 + 1);
      fields.add(tf);
    }

    themeButton = new Button("Switch to Jungle Theme");
    styleSetupButton(themeButton);
    themeButton.setOnAction(e -> switchTheme());

    saveButton = new Button("Save Player Names");
    styleSetupButton(saveButton);
    saveButton.setOnAction(e -> savePlayerNames());

    // --- Load Players Button ---
    loadButton = new Button("Load Player Names"); // Initialize new button
    styleSetupButton(loadButton);
    loadButton.setOnAction(e -> loadPlayerNames()); // Set its action

    Button startBtn = new Button("START ADVENTURE");
    startBtn.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 24));
    startBtn.setTextFill(Color.WHITE);
    startBtn.setPadding(new Insets(10, 40, 10, 40));
    startBtn.setBackground(new Background(new BackgroundFill(
        Color.web("#b8860b"), new CornerRadii(8), Insets.EMPTY)));
    startBtn.setEffect(new DropShadow(5, Color.BLACK));
    startBtn.setOnAction(e -> {
      List<String> names = collectPlayerNames();
      if (names.size() < 2) {
        showAlert(Alert.AlertType.ERROR, "Validation Error",
            "Enter at least two legendary names.");
      } else {
        onStart.accept(names, currentTheme);
      }
    });

    Image homeImg = loadImage("images/home_icon.png");
    ImageView homeView = new ImageView(homeImg);
    homeView.setFitWidth(40);
    homeView.setPreserveRatio(true);
    Button homeBtn = new Button();
    homeBtn.setGraphic(homeView);
    homeBtn.setBackground(Background.EMPTY);
    homeBtn.setOnAction(e -> onHome.run());

    // --- Layout Adjustments ---
    // Group utility buttons including the new Load button
    HBox topUtilityButtons = new HBox(20, themeButton); // Theme button can be separate or grouped
    topUtilityButtons.setAlignment(Pos.CENTER);

    HBox fileButtons = new HBox(20, saveButton, loadButton); // Save and Load buttons together
    fileButtons.setAlignment(Pos.CENTER);

    // Add new button group to the main content VBox
    VBox content = new VBox(20, title, grid, topUtilityButtons, fileButtons, startBtn); // Adjusted spacing and order
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(20)); // Add some padding around content

    StackPane root = new StackPane(bgView, content, homeBtn);
    StackPane.setAlignment(homeBtn, Pos.TOP_LEFT);
    StackPane.setMargin(homeBtn, new Insets(20));

    scene = new Scene(root, bgImg.getWidth(), bgImg.getHeight());
    UiStyles.apply(scene);
  }

  @Override
  public Scene getScene() {
    return scene;
  }

  @Override
  public void onShow() {
    // Optional: Clear fields when scene is shown if desired
    // fields.forEach(tf -> tf.setText(""));
  }

  @Override
  public void onHide() {
  }

  private Image loadImage(String path) {
    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
    if (is == null) {
      System.err.println("Failed to load image resource: " + path);
      throw new RuntimeException("Image resource not found: " + path);
    }
    return new Image(is);
  }

  private void switchTheme() {
    currentTheme = (currentTheme == Theme.EGYPT) ? Theme.JUNGLE : Theme.EGYPT;
    updateThemeVisuals();
  }

  private void updateThemeVisuals() {
    try {
      if (currentTheme == Theme.JUNGLE) {
        bgView.setImage(loadImage(JUNGLE_BG_PATH));
        themeButton.setText("Switch to Egypt Theme");
      } else {
        bgView.setImage(loadImage(EGYPT_BG_PATH));
        themeButton.setText("Switch to Jungle Theme");
      }
    } catch (RuntimeException e) {
      showAlert(Alert.AlertType.ERROR, "Theme Error",
          "Could not load background for " + currentTheme + " theme. Check image path.");
    }
  }

  private void styleSetupButton(Button btn) {
    btn.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 16));
    btn.setTextFill(Color.web("#333333"));
    btn.setBackground(new Background(new BackgroundFill(
        Color.web("#f0e68c", 0.8), new CornerRadii(5), Insets.EMPTY)));
    btn.setPadding(new Insets(8, 20, 8, 20));
    btn.setEffect(new DropShadow(3, Color.GRAY));
    btn.setOnMouseEntered(
        e -> btn.setBackground(new Background(new BackgroundFill(
            Color.web("#d4af37", 0.9), new CornerRadii(5), Insets.EMPTY))));
    btn.setOnMouseExited(
        e -> btn.setBackground(new Background(new BackgroundFill(
            Color.web("#f0e68c", 0.8), new CornerRadii(5), Insets.EMPTY))));
  }

  private List<String> collectPlayerNames() {
    List<String> names = new ArrayList<>();
    for (TextField tf : fields) {
      String nm = tf.getText().trim();
      if (!nm.isEmpty()) {
        names.add(nm);
      }
    }
    return names;
  }

  private void savePlayerNames() {
    List<String> names = collectPlayerNames();
    if (names.isEmpty()) {
      showAlert(Alert.AlertType.INFORMATION, "No Names",
          "Please enter at least one player name to save.");
      return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Player List As");
    fileChooser.setInitialFileName("players.csv");
    fileChooser.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

    File chosenFile = fileChooser.showSaveDialog(stage);

    if (chosenFile != null) {
      List<Player> playerList = new ArrayList<>();
      try {
        Tile startTile = new Tile(0);
        for (String name : names) {
          playerList.add(new Player(name, startTile));
        }
        try (Writer writer = new FileWriter(chosenFile)) {
          PlayerCsvReaderWriter.writeAll(writer, playerList);
          showAlert(Alert.AlertType.INFORMATION, "Save Successful",
              "Player list saved to:\n" + chosenFile.getAbsolutePath());
        } catch (IOException | InvalidParameterException e) { // Catch multiple exceptions
          showAlert(Alert.AlertType.ERROR, "Save Error",
              "Could not write to file or data was invalid:\n" + e.getMessage());
        }
      } catch (InvalidParameterException e) {
        showAlert(Alert.AlertType.ERROR, "Error Creating Players",
            "Could not prepare player data for saving:\n" + e.getMessage());
      }
    }
  }

  /**
   * Handles loading player names from a CSV file and populating the TextFields.
   */
  private void loadPlayerNames() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load Player List From");
    fileChooser.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

    File chosenFile = fileChooser.showOpenDialog(stage); // Use showOpenDialog

    if (chosenFile != null) {
      try (Reader reader = new FileReader(chosenFile)) {
        List<Player> loadedPlayers = PlayerCsvReaderWriter.readAll(reader);

        // Clear existing text fields before populating
        for (TextField tf : fields) {
          tf.setText("");
        }

        // Populate text fields with loaded player names
        // Only loads up to the number of available text fields (max 4)
        for (int i = 0; i < loadedPlayers.size() && i < fields.size(); i++) {
          Player p = loadedPlayers.get(i);
          if (p != null && p.getName() != null) {
            fields.get(i).setText(p.getName());
          }
        }

        if (loadedPlayers.isEmpty()) {
          showAlert(Alert.AlertType.INFORMATION, "Load Info",
              "The selected file was empty or contained no valid player data.");
        } else {
          showAlert(Alert.AlertType.INFORMATION, "Load Successful",
              "Player names loaded from:\n" + chosenFile.getAbsolutePath());
        }

      } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "Load Error",
            "Could not read from file:\n" + e.getMessage());
      } catch (InvalidParameterException e) {
        // This exception can be thrown by PlayerCsvReaderWriter.readAll
        // if the CSV format is incorrect (e.g., non-integer tileId)
        showAlert(Alert.AlertType.ERROR, "Load Error",
            "Invalid data format in CSV file:\n" + e.getMessage());
      } catch (Exception e) { // Catch any other unexpected errors
        showAlert(Alert.AlertType.ERROR, "Load Error",
            "An unexpected error occurred during loading:\n" + e.getMessage());
      }
    }
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.initOwner(stage);
    alert.showAndWait();
  }
}