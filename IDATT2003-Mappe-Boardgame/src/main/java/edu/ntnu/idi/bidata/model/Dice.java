package edu.ntnu.idi.bidata.model;

import java.util.ArrayList;
import java.util.List;

public class Dice {
  private final List<Die> dice;

  public Dice(int numberOfDice) {
    this.dice = new ArrayList<>();
    for (int i = 0; i < numberOfDice; i++) {
      this.dice.add(new Die()); // Add a new Die object for each dice
    }
  }

  public int roll() {
    return dice.stream()
        .mapToInt(Die::roll)
        .sum();
  }

  public int getDie(int dieNumber) {
    return dice.get(dieNumber).getValue();
  }

}
