package edu.ntnu.idi.bidata.ui.sl;

import edu.ntnu.idi.bidata.controller.PlayerSetupController; // Import the new controller
import edu.ntnu.idi.bidata.exception.InvalidParameterException; // For catching
import edu.ntnu.idi.bidata.model.Player; // Still needed for DEFAULT_PIECE_IDENTIFIER

import edu.ntnu.idi.bidata.ui.PieceUIData;
import edu.ntnu.idi.bidata.ui.PlayerSetupData;
import edu.ntnu.idi.bidata.ui.SceneManager.ControlledScene;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Egypt/Jungle-themed player setup; allows theme switching, saving, and loading player names and pieces.
 * Passes selected theme and player setup data to the onStart callback.
 * Uses PlayerSetupController for save/load logic.
 */
public class SnakeLadderPlayerSetupScene implements ControlledScene {

  public enum Theme { EGYPT, JUNGLE }

  private final Scene scene;
  private final Stage stage;
  private final ImageView bgView;
  private final Button themeButton, saveButton, loadButton, startBtn, homeBtn;
  private final List<TextField> nameFields;
  private final List<ComboBox<PieceUIData>> pieceSelectors;
  private final PlayerSetupController setupController; // Controller for setup logic

  private Theme currentTheme = Theme.EGYPT;
  private static final String EGYPT_BG_PATH = "/images/player_setup_bg.png";
  private static final String JUNGLE_BG_PATH = "/images/jungle_setup_bg.png";
  private static final String HOME_ICON_PATH = "/images/home_icon.png";


  // Made public static final so other UI classes can access the defined pieces if needed (e.g. SnakeLadderGameScene, SnakeLadderBoardView)
  public static final List<PieceUIData> AVAILABLE_PIECES = List.of(
      new PieceUIData("king", "/images/piece_king.png"),
      new PieceUIData("queen", "/images/piece_queen.png"),
      new PieceUIData("rook", "/images/piece_rook.png"),
      new PieceUIData("knight", "/images/piece_knight.png")
  );

  public SnakeLadderPlayerSetupScene(Stage stage, BiConsumer<List<PlayerSetupData>, Theme> onStart, Runnable onHome) {
    this.stage = stage;
    this.setupController = new PlayerSetupController(); // Instantiate the controller
    this.nameFields = new ArrayList<>();
    this.pieceSelectors = new ArrayList<>();

    // --- Background ---
    Image bgImg = loadImage(EGYPT_BG_PATH); // Initial theme
    bgView = new ImageView(bgImg);
    bgView.fitWidthProperty().bind(stage.widthProperty());
    bgView.fitHeightProperty().bind(stage.heightProperty());
    bgView.setPreserveRatio(false);

    // --- Title ---
    Label title = new Label("ENTER YOUR LEGENDS' NAMES & PIECES");
    title.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 36));
    title.setTextFill(Color.web("#d4af37"));
    title.setEffect(new DropShadow(3, Color.BLACK));

    // --- Player Input Grid ---
    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);
    grid.setAlignment(Pos.CENTER);

    for (int i = 0; i < 4; i++) { // Assuming max 4 players can be configured
      Label lbl = new Label("Player " + (i + 1));
      lbl.setFont(Font.font("Trajan Pro", FontWeight.SEMI_BOLD, 20));
      lbl.setTextFill(Color.web("#f0e68c"));

      TextField tf = new TextField();
      tf.setPromptText("Name...");
      tf.setFont(Font.font(18));
      tf.setPrefWidth(220);
      tf.setStyle("-fx-prompt-text-fill: gray; -fx-text-fill: cyan; -fx-border-color: cyan; -fx-border-radius:5; -fx-background-radius:5; -fx-background-color: rgba(0,0,0,0.4);");
      tf.setEffect(new DropShadow(10, Color.CYAN));
      nameFields.add(tf);

      ComboBox<PieceUIData> pieceBox = new ComboBox<>(FXCollections.observableArrayList(AVAILABLE_PIECES));
      if (!AVAILABLE_PIECES.isEmpty()) pieceBox.setValue(AVAILABLE_PIECES.getFirst());
      pieceBox.setPrefWidth(180);
      pieceBox.setStyle("-fx-font-size: 14px; -fx-control-inner-background: rgba(0,0,0,0.4); -fx-text-fill: cyan; -fx-border-color: cyan; -fx-border-radius:5; -fx-prompt-text-fill: #a0a0a0;"); // Added prompt text fill
      pieceBox.setEffect(new DropShadow(10, Color.CYAN));
      pieceBox.setCellFactory(param -> new PieceListCell());
      pieceBox.setButtonCell(new PieceListCell());
      pieceSelectors.add(pieceBox);

      VBox playerEntryBox = new VBox(5, lbl, new HBox(10, tf, pieceBox));
      playerEntryBox.setAlignment(Pos.CENTER_LEFT);
      grid.add(playerEntryBox, i % 2, i / 2); // Arrange in 2x2 grid
    }

    // --- Control Buttons ---
    themeButton = new Button("Switch to Jungle Theme");
    styleSetupButton(themeButton);
    themeButton.setOnAction(e -> switchTheme());

    saveButton = new Button("Save Player Setup");
    styleSetupButton(saveButton);
    saveButton.setOnAction(e -> handleSavePlayerSetup());

    loadButton = new Button("Load Player Setup");
    styleSetupButton(loadButton);
    loadButton.setOnAction(e -> handleLoadPlayerSetup());

    startBtn = new Button("START ADVENTURE");
    startBtn.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 24));
    startBtn.setTextFill(Color.WHITE);
    startBtn.setPadding(new Insets(10, 40, 10, 40));
    startBtn.setBackground(new Background(new BackgroundFill(Color.web("#b8860b"), new CornerRadii(8), Insets.EMPTY)));
    startBtn.setEffect(new DropShadow(5, Color.BLACK));
    startBtn.setOnAction(e -> {
      List<PlayerSetupData> setupData = collectPlayerInputs();
      // Assuming at least 1 player is required, adjust if 2 is minimum for your game
      if (setupData.isEmpty() || setupData.stream().allMatch(psd -> psd.name().isBlank())) {
        showAlert(Alert.AlertType.ERROR, "Validation Error", "Enter at least one legendary name and select a piece.");
      } else {
        onStart.accept(setupData, currentTheme);
      }
    });

    // --- Home Button ---
    Image homeImg = loadImage(HOME_ICON_PATH);
    ImageView homeView = new ImageView(homeImg);
    homeView.setFitWidth(40);
    homeView.setPreserveRatio(true);
    homeBtn = new Button("", homeView); // Set graphic, no text
    homeBtn.setBackground(Background.EMPTY);
    homeBtn.setOnAction(e -> onHome.run());

    // --- Layout Assembly ---
    HBox topUtilityButtons = new HBox(20, themeButton);
    topUtilityButtons.setAlignment(Pos.CENTER);
    HBox fileButtons = new HBox(20, saveButton, loadButton);
    fileButtons.setAlignment(Pos.CENTER);

    VBox content = new VBox(25, title, grid, topUtilityButtons, fileButtons, startBtn);
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(30));

    StackPane root = new StackPane(bgView, content);
    StackPane.setAlignment(homeBtn, Pos.TOP_LEFT);
    StackPane.setMargin(homeBtn, new Insets(20));
    root.getChildren().add(homeBtn);

    scene = new Scene(root, 1024, 768); // Example initial size
    // UiStyles.apply(scene); // If you have a UiStyles class for global styling
  }

  @Override
  public Scene getScene() {
    return scene;
  }

  @Override
  public void onShow() {
    // Reset fields when the scene is shown
    nameFields.forEach(tf -> tf.setText(""));
    pieceSelectors.forEach(cb -> {
      if (!AVAILABLE_PIECES.isEmpty()) {
        cb.setValue(AVAILABLE_PIECES.getFirst());
      } else {
        cb.setValue(null); // Handle case where no pieces are defined
      }
    });
  }

  @Override
  public void onHide() {
    // Optional: Any cleanup when the scene is hidden
  }

  private Image loadImage(String path) {
    try {
      InputStream is = getClass().getResourceAsStream(path);
      // A common issue: if path is "/images/file.png", getResourceAsStream sometimes prefers "images/file.png"
      // or if path is "images/file.png", it might need "/images/file.png".
      // The following tries to be robust.
      if (is == null && path.startsWith("/")) {
        is = getClass().getResourceAsStream(path.substring(1));
      } else if (is == null && !path.startsWith("/")) {
        is = getClass().getResourceAsStream("/" + path);
      }
      Objects.requireNonNull(is, "Cannot load image resource from path: " + path);
      return new Image(is);
    } catch (NullPointerException e) {
      System.err.println("Error: Image resource not found at path: " + path + ". Check if the path is correct and the resource exists.");
      throw new RuntimeException("Failed to load critical image: " + path, e);
    } catch (Exception e) {
      System.err.println("An unexpected error occurred while loading image: " + path);
      e.printStackTrace();
      throw new RuntimeException("Failed to load critical image: " + path, e);
    }
  }

  private void switchTheme() {
    currentTheme = (currentTheme == Theme.EGYPT) ? Theme.JUNGLE : Theme.EGYPT;
    updateThemeVisuals();
  }

  private void updateThemeVisuals() {
    try {
      String bgPath = (currentTheme == Theme.JUNGLE) ? JUNGLE_BG_PATH : EGYPT_BG_PATH;
      bgView.setImage(loadImage(bgPath));
      themeButton.setText((currentTheme == Theme.JUNGLE) ? "Switch to Egypt Theme" : "Switch to Jungle Theme");
    } catch (Exception e) { // Catch RuntimeException from loadImage or others
      showAlert(Alert.AlertType.ERROR, "Theme Error", "Could not load background for " + currentTheme + " theme.\n" + e.getMessage());
    }
  }

  private void styleSetupButton(Button btn) {
    btn.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 16));
    btn.setTextFill(Color.web("#333333"));
    btn.setBackground(new Background(new BackgroundFill(Color.web("#f0e68c", 0.8), new CornerRadii(5), Insets.EMPTY)));
    btn.setPadding(new Insets(8, 20, 8, 20));
    btn.setEffect(new DropShadow(3, Color.GRAY));
    btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(Color.web("#d4af37", 0.9), new CornerRadii(5), Insets.EMPTY))));
    btn.setOnMouseExited(e -> btn.setBackground(new Background(new BackgroundFill(Color.web("#f0e68c", 0.8), new CornerRadii(5), Insets.EMPTY))));
  }

  private List<PlayerSetupData> collectPlayerInputs() {
    List<PlayerSetupData> data = new ArrayList<>();
    for (int i = 0; i < nameFields.size(); i++) {
      String name = nameFields.get(i).getText().trim();
      PieceUIData selectedPiece = pieceSelectors.get(i).getValue();

      if (!name.isEmpty()) { // Only add player if name is provided
        String pieceId = Player.DEFAULT_PIECE_IDENTIFIER; // Fallback
        if (selectedPiece != null) {
          pieceId = selectedPiece.getIdentifier();
        } else if (!AVAILABLE_PIECES.isEmpty()) {
          // If nothing selected but pieces are available, use first as default
          pieceId = AVAILABLE_PIECES.getFirst().getIdentifier();
        }
        data.add(new PlayerSetupData(name, pieceId));
      }
    }
    return data;
  }

  private void handleSavePlayerSetup() {
    List<PlayerSetupData> playerInputs = collectPlayerInputs();
    if (playerInputs.isEmpty()) {
      showAlert(Alert.AlertType.INFORMATION, "No Data", "Please enter at least one player name to save.");
      return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Player Setup As");
    fileChooser.setInitialFileName("sl_players.csv");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
    File chosenFile = fileChooser.showSaveDialog(stage);

    if (chosenFile != null) {
      try {
        boolean success = setupController.savePlayerSetup(playerInputs, chosenFile);
        if (success) {
          showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Player setup saved to:\n" + chosenFile.getAbsolutePath());
        }
        // The controller now throws exceptions for explicit failure cases, so 'else' might not be needed
        // if all failure paths in controller throw.
      } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "Save Error", "Could not write to file: " + e.getMessage());
      } catch (InvalidParameterException e) { // From Player constructor within controller
        showAlert(Alert.AlertType.ERROR, "Data Error", "Invalid player data for saving: " + e.getMessage());
      } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Unexpected Save Error", "An unexpected error occurred: " + e.getMessage());
        e.printStackTrace(); // For debugging
      }
    }
  }

  private void handleLoadPlayerSetup() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load Player Setup From");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
    File chosenFile = fileChooser.showOpenDialog(stage);

    if (chosenFile != null) {
      try {
        List<PlayerSetupData> loadedData = setupController.loadPlayerSetup(chosenFile);

        nameFields.forEach(tf -> tf.setText(""));
        pieceSelectors.forEach(cb -> { if (!AVAILABLE_PIECES.isEmpty()) cb.setValue(AVAILABLE_PIECES.getFirst()); });

        for (int i = 0; i < loadedData.size() && i < nameFields.size(); i++) {
          PlayerSetupData data = loadedData.get(i);
          nameFields.get(i).setText(data.name());

          Optional<PieceUIData> pieceOpt = AVAILABLE_PIECES.stream()
              .filter(pd -> pd.getIdentifier().equals(data.pieceIdentifier()))
              .findFirst();

          if (pieceOpt.isPresent()) {
            pieceSelectors.get(i).setValue(pieceOpt.get());
          } else if (!AVAILABLE_PIECES.isEmpty()) {
            pieceSelectors.get(i).setValue(AVAILABLE_PIECES.getFirst());
            System.err.println("Loaded piece ID '" + data.pieceIdentifier() + "' for player '" + data.name() + "' not found in current piece list. Using default.");
          }
        }

        if (loadedData.isEmpty() && chosenFile.length() > 0) { // File not empty but no valid data parsed
          showAlert(Alert.AlertType.WARNING, "Load Info", "The selected file might be improperly formatted or contain no valid player data.");
        } else if (loadedData.isEmpty()) { // File was actually empty
          showAlert(Alert.AlertType.INFORMATION, "Load Info", "The selected file was empty.");
        }
        else {
          showAlert(Alert.AlertType.INFORMATION, "Load Successful", "Player setup loaded from:\n" + chosenFile.getAbsolutePath());
        }

      } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "Load Error", "Could not read from file: " + e.getMessage());
      } catch (InvalidParameterException e) { // From PlayerCsvReaderWriter via controller
        showAlert(Alert.AlertType.ERROR, "File Format Error", "Invalid data format in CSV file: " + e.getMessage());
      } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Unexpected Load Error", "An unexpected error occurred during loading: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null); // No header text
    alert.setContentText(content);
    alert.initOwner(stage); // Ensures alert is modal to this stage
    alert.showAndWait();
  }

  // Custom ListCell for ComboBox to display image and text for PieceUIData
  private static class PieceListCell extends ListCell<PieceUIData> {
    private final ImageView imageView = new ImageView();

    public PieceListCell() {
      imageView.setPreserveRatio(true);
      // Set a fixed height for the image view in the dropdown list and button cell
      imageView.setFitHeight(PieceUIData.COMBOBOX_IMAGE_SIZE); // Use constant from PieceUIData
      imageView.setFitWidth(PieceUIData.COMBOBOX_IMAGE_SIZE);  // Use constant from PieceUIData
    }

    @Override
    protected void updateItem(PieceUIData item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
      } else {
        setText(item.toString()); // Displays "King", "Queen", etc.
        Image pieceImage = item.getComboBoxImage(); // Get specifically sized image
        if (pieceImage != null) {
          imageView.setImage(pieceImage);
          setGraphic(imageView);
        } else {
          // Fallback if image is null (e.g., loading error)
          setGraphic(null); // Or a placeholder graphic
          System.err.println("ComboBox: Image for piece " + item.getIdentifier() + " is null.");
        }
        setContentDisplay(javafx.scene.control.ContentDisplay.LEFT); // Image to the left of text
        setMinWidth(Region.USE_PREF_SIZE); // Ensure cell takes up preferred size
      }
    }
  }
}