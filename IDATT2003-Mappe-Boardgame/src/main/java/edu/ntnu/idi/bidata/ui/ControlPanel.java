package edu.ntnu.idi.bidata.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Contains controls: roll dice button and status label.
 */
public class ControlPanel extends VBox {
  private final Button rollButton;
  private final Label statusLabel;

  public ControlPanel() {
    setPadding(new Insets(10));
    setSpacing(10);

    rollButton = new Button("Roll Dice");
    statusLabel = new Label("Welcome!");

    getChildren().addAll(rollButton, statusLabel);
  }

  public Button getRollButton() {
    return rollButton;
  }

  public void setStatus(String text) {
    statusLabel.setText(text);
  }
}