package edu.ntnu.idi.bidata.model;

import java.util.ArrayList;
import java.util.List;
import edu.ntnu.idi.bidata.exception.InvalidParameterException;

/**
 * Aggregates multiple Die instances and rolls them together.
 */
public class Dice {
  private final List<Die> dice;

  /**
   * Creates a collection of the given number of dice.
   * @param numberOfDice must be >0
   */
  public Dice(int numberOfDice) {
    if (numberOfDice < 1) {
      throw new InvalidParameterException("Must create at least one die");
    }
    dice = new ArrayList<>(numberOfDice);
    for (int i = 0; i < numberOfDice; i++) {
      dice.add(new Die());
    }
  }

  /**
   * Rolls all dice and returns the sum of their face values.
   */
  public int roll() {
    return dice.stream()
        .mapToInt(Die::roll)
        .sum();
  }

  /**
   * Returns last rolled value of a specific die (0‐based index).
   */
  public int getDie(int index) {
    if (index < 0 || index >= dice.size()) {
      throw new InvalidParameterException("Die index out of range: " + index);
    }
    return dice.get(index).getValue();
  }
}

