package edu.ntnu.idi.bidata;

import edu.ntnu.idi.bidata.controller.WindowController;
import edu.ntnu.idi.bidata.model.BoardGame;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    WindowController WindowController = new WindowController();
    // Create a layout
    Group root = WindowController.WriteToScreen("Round 1", primaryStage);

    // Set the scene
    Scene scene = new Scene(root, 300, 250);

    // Set the stage (window)
    primaryStage.setTitle("JavaFX App");
    primaryStage.setScene(scene);
    primaryStage.show();




//
//    scene.setRoot(WindowController.WriteToScreen("Round 2"));
  }

  public static void main(String[] args) {
    launch(args);
  }
}
