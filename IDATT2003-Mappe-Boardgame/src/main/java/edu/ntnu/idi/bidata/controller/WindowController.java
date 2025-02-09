package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.BoardGame;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class WindowController {
    int round = 0;
    String[] game = BoardGame.generateExampleGame();

    public Group WriteToScreen(String message, javafx.stage.Stage primaryStage) {
        Text text = new Text();

        //Setting the text to be added.
        text.setText(message);

        //setting the position of the text
        text.setX(50);
        text.setY(50);

        // Create a button with text
        Button btn = new Button();
        btn.setText("Next Round");

        btn.setLayoutX(50);
        btn.setLayoutY(100);

        btn.setOnAction(value ->  {
            this.WriteToScreenUpdate("AAAAa", primaryStage);
        });


        //Creating a Group object

        return new Group(text, btn);
    }

    public void WriteToScreenUpdate(String message, javafx.stage.Stage primaryStage) {
        Text text = new Text();

        //Setting the text to be added.
        text.setText(message);

        //setting the position of the text
        text.setX(50);
        text.setY(50);

        // Create a button with text
        Button btn = new Button();
        btn.setText("Next Round");

        btn.setLayoutX(50);
        btn.setLayoutY(100);

        btn.setOnAction(value ->  {
            this.round++;
            this.WriteToScreenUpdate(this.game[this.round], primaryStage);
        });

        //Creating a Group object
        Group root = new Group(text, btn);

        primaryStage.getScene().setRoot(root);



    }


}
