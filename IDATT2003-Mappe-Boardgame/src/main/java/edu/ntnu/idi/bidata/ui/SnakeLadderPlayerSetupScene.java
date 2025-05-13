package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.file.PlayerCsvReaderWriter;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SnakeLadderPlayerSetupScene implements SceneManager.ControlledScene {

  public enum Theme { EGYPT, JUNGLE }

  private final Scene scene;
  private final Stage stage;
  private final ImageView bgView;
  private final Button themeButton, saveButton, loadButton;
  private final List<TextField> nameFields;
  private final List<ComboBox<PieceUIData>> pieceSelectors;

  private Theme currentTheme = Theme.EGYPT;
  private static final String EGYPT_BG_PATH = "/images/player_setup_bg.png"; // Assuming leading / for classpath root
  private static final String JUNGLE_BG_PATH = "/images/jungle_setup_bg.png";

  public static final List<PieceUIData> AVAILABLE_PIECES = List.of(
      new PieceUIData("king", "/images/piece_king.png"),
      new PieceUIData("queen", "/images/piece_queen.png"),
      new PieceUIData("rook", "/images/piece_rook.png"),
      new PieceUIData("knight", "/images/piece_knight.png")
  );

  public SnakeLadderPlayerSetupScene(Stage stage, BiConsumer<List<PlayerSetupData>, Theme> onStart, Runnable onHome) {
    this.stage = stage;
    this.nameFields = new ArrayList<>();
    this.pieceSelectors = new ArrayList<>();

    Image bgImg = loadImage(EGYPT_BG_PATH);
    bgView = new ImageView(bgImg);
    bgView.fitWidthProperty().bind(stage.widthProperty());
    bgView.fitHeightProperty().bind(stage.heightProperty());
    bgView.setPreserveRatio(false);

    Label title = new Label("ENTER YOUR LEGENDS' NAMES & PIECES");
    title.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 36));
    title.setTextFill(Color.web("#d4af37"));
    title.setEffect(new DropShadow(3, Color.BLACK));

    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);
    grid.setAlignment(Pos.CENTER);

    for (int i = 0; i < 4; i++) {
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
      pieceBox.setStyle("-fx-font-size: 14px; -fx-control-inner-background: rgba(0,0,0,0.4); -fx-text-fill: cyan; -fx-border-color: cyan; -fx-border-radius:5;");
      pieceBox.setEffect(new DropShadow(10, Color.CYAN));
      pieceBox.setCellFactory(param -> new PieceListCell());
      pieceBox.setButtonCell(new PieceListCell());
      pieceSelectors.add(pieceBox);

      VBox playerEntryBox = new VBox(5, lbl, new HBox(10, tf, pieceBox));
      playerEntryBox.setAlignment(Pos.CENTER_LEFT);
      grid.add(playerEntryBox, i % 2, i / 2);
    }

    themeButton = new Button("Switch to Jungle Theme");
    styleSetupButton(themeButton);
    themeButton.setOnAction(e -> switchTheme());

    saveButton = new Button("Save Player Setup");
    styleSetupButton(saveButton);
    saveButton.setOnAction(e -> savePlayerSetup());

    loadButton = new Button("Load Player Setup");
    styleSetupButton(loadButton);
    loadButton.setOnAction(e -> loadPlayerSetup());

    Button startBtn = new Button("START ADVENTURE");
    startBtn.setFont(Font.font("Trajan Pro", FontWeight.BOLD, 24));
    startBtn.setTextFill(Color.WHITE);
    startBtn.setPadding(new Insets(10, 40, 10, 40));
    startBtn.setBackground(new Background(new BackgroundFill(Color.web("#b8860b"), new CornerRadii(8), Insets.EMPTY)));
    startBtn.setEffect(new DropShadow(5, Color.BLACK));
    startBtn.setOnAction(e -> {
      List<PlayerSetupData> setupData = collectPlayerInputs();
      if (setupData.size() < 2) {
        showAlert(Alert.AlertType.ERROR, "Validation Error", "Enter at least one legendary name and select a piece.");
      } else {
        onStart.accept(setupData, currentTheme);
      }
    });

    Image homeImg = loadImage("/images/home_icon.png");
    ImageView homeView = new ImageView(homeImg);
    homeView.setFitWidth(40);
    homeView.setPreserveRatio(true);
    Button homeBtn = new Button("", homeView);
    homeBtn.setBackground(Background.EMPTY);
    homeBtn.setOnAction(e -> onHome.run());

    HBox topUtil = new HBox(20, themeButton); topUtil.setAlignment(Pos.CENTER);
    HBox fileBtns = new HBox(20, saveButton, loadButton); fileBtns.setAlignment(Pos.CENTER);
    VBox content = new VBox(25, title, grid, topUtil, fileBtns, startBtn);
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(30));

    StackPane root = new StackPane(bgView, content);
    StackPane.setAlignment(homeBtn, Pos.TOP_LEFT);
    StackPane.setMargin(homeBtn, new Insets(20));
    root.getChildren().add(homeBtn);

    scene = new Scene(root, 1024, 768);
    // UiStyles.apply(scene); // Assuming UiStyles exists
  }

  @Override public Scene getScene() { return scene; }
  @Override public void onShow() {
    nameFields.forEach(tf -> tf.setText(""));
    pieceSelectors.forEach(cb -> { if (!AVAILABLE_PIECES.isEmpty()) cb.setValue(AVAILABLE_PIECES.getFirst()); });
  }

  private Image loadImage(String path) {
    try {
      InputStream is = getClass().getResourceAsStream(path);
      if (is == null && !path.startsWith("/")) {
        is = getClass().getResourceAsStream("/" + path);
      }
      Objects.requireNonNull(is, "Cannot load image resource: " + path);
      return new Image(is);
    } catch (Exception e) {
      System.err.println("Failed to load image: " + path);
      e.printStackTrace();
      // Return a placeholder image or throw error to make issue visible
      // For now, throw an error to ensure paths are fixed.
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
    } catch (Exception e) {
      showAlert(Alert.AlertType.ERROR, "Theme Error", "Could not load background for " + currentTheme + " theme.\n" + e.getMessage());
    }
  }

  private void styleSetupButton(Button btn) { /* ... Same as before ... */
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
      if (!name.isEmpty() && selectedPiece != null) {
        data.add(new PlayerSetupData(name, selectedPiece.getIdentifier()));
      } else if (!name.isEmpty()) { // Name entered, but piece somehow not selected
        data.add(new PlayerSetupData(name, Player.DEFAULT_PIECE_IDENTIFIER)); // Fallback
      }
    }
    return data;
  }

  private void savePlayerSetup() {
    List<PlayerSetupData> inputs = collectPlayerInputs();
    if (inputs.isEmpty()) { /* ... alert ... */ return; }
    FileChooser fc = new FileChooser(); /* ... setup ... */
    File file = fc.showSaveDialog(stage);
    if (file == null) return;
    try (Writer w = new FileWriter(file)) {
      List<Player> playersToSave = new ArrayList<>();
      Tile placeholderTile = new Tile(0);
      for (PlayerSetupData psd : inputs) {
        playersToSave.add(new Player(psd.name(), placeholderTile, psd.pieceIdentifier()));
      }
      PlayerCsvReaderWriter.writeAll(w, playersToSave);
      showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Player setup saved.");
    } catch (IOException | InvalidParameterException e) {
      showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save player setup: " + e.getMessage());
    }
  }

  private void loadPlayerSetup() {
    FileChooser fc = new FileChooser(); /* ... setup ... */
    File file = fc.showOpenDialog(stage);
    if (file == null) return;
    try (Reader r = new FileReader(file)) {
      List<Player> loadedPlayers = PlayerCsvReaderWriter.readAll(r);
      nameFields.forEach(tf -> tf.setText(""));
      pieceSelectors.forEach(cb -> { if (!AVAILABLE_PIECES.isEmpty()) cb.setValue(AVAILABLE_PIECES.getFirst()); });

      for (int i = 0; i < loadedPlayers.size() && i < nameFields.size(); i++) {
        Player p = loadedPlayers.get(i);
        nameFields.get(i).setText(p.getName());
        Optional<PieceUIData> pieceOpt = AVAILABLE_PIECES.stream()
            .filter(pd -> pd.getIdentifier().equals(p.getPieceIdentifier()))
            .findFirst();
        if (pieceOpt.isPresent()) {
          pieceSelectors.get(i).setValue(pieceOpt.get());
        } else if (!AVAILABLE_PIECES.isEmpty()) {
          pieceSelectors.get(i).setValue(AVAILABLE_PIECES.getFirst()); // Fallback
          System.err.println("Loaded piece ID '" + p.getPieceIdentifier() + "' not found. Using default.");
        }
      }
      showAlert(Alert.AlertType.INFORMATION, "Load Successful", "Player setup loaded.");
    } catch (IOException | InvalidParameterException e) {
      showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load player setup: " + e.getMessage());
    }
  }

  private void showAlert(Alert.AlertType type, String title, String content) { /* ... Same ... */
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.initOwner(stage);
    alert.showAndWait();
  }

  // Custom ListCell for ComboBox to display image and text
  private static class PieceListCell extends ListCell<PieceUIData> {
    private final ImageView imageView = new ImageView();
    public PieceListCell() {
      imageView.setPreserveRatio(true);
    }
    @Override
    protected void updateItem(PieceUIData item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
      } else {
        setText(item.toString());
        imageView.setImage(item.getComboBoxImage());
        setGraphic(imageView);
      }
    }
  }
}