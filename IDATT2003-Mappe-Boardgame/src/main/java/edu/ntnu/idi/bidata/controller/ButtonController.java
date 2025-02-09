package edu.ntnu.idi.bidata.controller;

import javafx.event.Event;
import javafx.scene.control.Button;

public class ButtonController {
    Button button;

    public ButtonController(Button button) {
        this.button = button;
    }

    public void OnActionEvent(Event event) {
        System.out.println("Button clicked");
    }

}
