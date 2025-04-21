package edu.ntnu.idi.bidata.model;

import java.util.Random;

/**
 * Represents a single six‐sided die.
 */
public class Die {
  private static final int SIDES = 6;
  private int lastRolledValue;
  private final Random random = new Random();

  /**
   * Rolls this die, updates and returns the face value (1–6).
   * @return rolled value
   */
  public int roll() {
    lastRolledValue = random.nextInt(SIDES) + 1;
    return lastRolledValue;
  }

  /**
   * Returns the last rolled value (undefined before first roll).
   */
  public int getValue() {
    return lastRolledValue;
  }
}
