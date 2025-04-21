package edu.ntnu.idi.bidata.exception;

/**
 * Thrown when JSON parsing or serialization fails.
 */
public class JsonParseException extends RuntimeException {
  public JsonParseException(String message) {
    super(message);
  }

  public JsonParseException(String message, Throwable cause) {
    super(message, cause);
  }
}