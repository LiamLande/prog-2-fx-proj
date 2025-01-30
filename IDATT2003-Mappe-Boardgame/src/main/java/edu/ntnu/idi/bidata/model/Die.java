package edu.ntnu.idi.bidata.model;

import java.util.Random;

public class Die {
  private int lastRolledValue;
  Random ran = new Random();

  public int roll() {
    lastRolledValue = ran.nextInt(6) + 1;
    return lastRolledValue;
  }

  public int getValue() {
    return lastRolledValue;
  }
}
