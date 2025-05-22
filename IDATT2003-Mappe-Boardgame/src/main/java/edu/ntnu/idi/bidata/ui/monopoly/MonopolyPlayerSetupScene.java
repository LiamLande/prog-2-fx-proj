package edu.ntnu.idi.bidata.ui.monopoly;

import edu.ntnu.idi.bidata.ui.SceneManager.ControlledScene;
import edu.ntnu.idi.bidata.ui.UiStyles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Creates and manages the player setup scene for a Monopoly game.
 * This scene allows users to enter names for 2 to 4 players and then
 * initiates the game or returns to the home screen.
 * It features a reactive background that adjusts to the window size.
 */
public class MonopolyPlayerSetupScene implements ControlledScene {
  private final Scene scene;

  /**
   * Constructs the MonopolyPlayerSetupScene.
   *
   * @param stage The primary stage of the application, used for binding background size and owner for dialogs.
   * @param onStart A {@link Consumer} that accepts a list of player names and is called when the game is started.
   * @param onHome A {@link Runnable} that is executed when the user chooses to return to the home screen.
   */
  public MonopolyPlayerSetupScene(Stage stage,
      Consumer<List<String>> onStart,
      Runnable onHome) {

    Image bgImg = loadImage("/images/monopoly_setup_bg.png");
    ImageView bgView = new ImageView(bgImg);

    //Make background reactive to window size
    bgView.setSmooth(true);
    bgView.fitWidthProperty().bind(stage.widthProperty());
    bgView.fitHeightProperty().bind(stage.heightProperty());
    bgView.setPreserveRatio(false);

    Label title = new Label("ENTER YOUR INVESTORS' NAMES");
    title.getStyleClass().add("scene-title");

    GridPane grid = new GridPane();
    grid.setHgap(40);
    grid.setVgap(30);
    grid.setAlignment(Pos.CENTER);

    List<TextField> fields = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Label lbl = new Label("Player " + (i + 1));
      lbl.getStyleClass().add("player-label");

      TextField tf = new TextField();
      tf.setPromptText("Name...");
      tf.getStyleClass().add("player-input");
      tf.setFont(Font.font(16));
      tf.setPrefWidth(260);

      int row = i < 2 ? 0 : 1;
      int col = i % 2;
      grid.add(lbl, col, row * 2);      // Label in first sub-row
      grid.add(tf,  col, row * 2 + 1);  // TextField in second sub-row
      fields.add(tf);
    }

    Button startBtn = new Button("LAUNCH MARKET");
    startBtn.getStyleClass().add("start-button");
    startBtn.setOnAction(e -> {
      List<String> names = new ArrayList<>();
      for (TextField tf : fields) {
        String nm = tf.getText().trim();
        if (!nm.isEmpty()) {
          names.add(nm);
        }
      }

      if (names.size() < 2) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Input Error");
        a.setHeaderText(null);
        a.setContentText("Please enter at least two investor names to start the game.");
        a.initOwner(stage);
        a.showAndWait();
      } else {
        onStart.accept(names);
      }
    });

    Image homeImg = loadImage("/images/home_icon.png");
    ImageView homeView = new ImageView(homeImg);
    homeView.setFitWidth(40);
    homeView.setPreserveRatio(true);
    Button homeBtn = new Button();
    homeBtn.setGraphic(homeView);
    homeBtn.setBackground(Background.EMPTY);
    homeBtn.setOnAction(e -> onHome.run());

    VBox content = new VBox(30, title, grid, startBtn);
    content.setAlignment(Pos.CENTER);

    StackPane root = new StackPane();
    root.getChildren().addAll(bgView, content);

    // Add homeBtn to the root StackPane and align it
    root.getChildren().add(homeBtn);
    StackPane.setAlignment(homeBtn, Pos.TOP_LEFT);
    StackPane.setMargin(homeBtn, new Insets(20));

    // Scene is created with the root. Size will be determined by the stage initially.
    scene = new Scene(root);
    UiStyles.apply(scene);
  }

  /**
   * Gets the JavaFX {@link Scene} for the player setup.
   * This scene contains all UI elements for player name input and game start/home navigation.
   *
   * @return The {@link Scene} object representing the player setup interface.
   */
  @Override
  public Scene getScene() {
    return this.scene;
  }

  private Image loadImage(String resourcePath) {
    // More robust image loading
    String correctedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
    InputStream is = getClass().getResourceAsStream(correctedPath);

    if (is == null) {
      is = getClass().getClassLoader().getResourceAsStream(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
    }

    Objects.requireNonNull(is, "Cannot load image resource from path: " + resourcePath +
        ". Corrected attempted path: " + correctedPath +
        ". Please ensure the image is in the correct resources folder and the path is correct (e.g., '/images/your_image.png').");
    return new Image(is);
  }
}