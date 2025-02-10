package edu.ntnu.idi.bidata;

import edu.ntnu.idi.bidata.controller.GameController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    GameController controller = new GameController();
    Scene scene = new Scene(controller.getView(), 400, 300);

    primaryStage.setTitle("Snakes and Ladders");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
