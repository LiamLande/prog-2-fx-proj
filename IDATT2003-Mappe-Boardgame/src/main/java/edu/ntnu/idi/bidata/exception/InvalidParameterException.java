package edu.ntnu.idi.bidata.exception;

/**
 * Thrown when a method receives invalid parameters.
 */
public class InvalidParameterException extends RuntimeException {
  public InvalidParameterException(String message) {
    super(message);
  }

  public InvalidParameterException(String message, Throwable cause) {
    super(message, cause);
  }
}