package edu.ntnu.idi.bidata;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    // Create a layout
    StackPane root = new StackPane();

    // Set the scene
    Scene scene = new Scene(root, 300, 250);

    // Set the stage (window)
    primaryStage.setTitle("JavaFX App");
    primaryStage.setScene(scene);
    primaryStage.show();

    BoardGame game = new BoardGame();
    game.createBoard();
    game.createDice();
    game.addPlayer(new Player("Alice", game));
    game.addPlayer(new Player("Nick", game));
    game.play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}