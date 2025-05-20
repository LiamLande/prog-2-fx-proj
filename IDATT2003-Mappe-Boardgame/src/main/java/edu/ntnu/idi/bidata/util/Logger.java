package edu.ntnu.idi.bidata.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple static logger class for console output.
 * Provides INFO, DEBUG, WARNING, and ERROR logging levels.
 * Timestamps and log levels are automatically prepended to messages.
 * Error messages can include stack traces.
 */
public final class Logger { // final to prevent inheritance

  private enum LogLevel {
    INFO, DEBUG, WARNING, ERROR
  }

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  // Private constructor to prevent instantiation of this utility class
  private Logger() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  private static void log(LogLevel level, String message, Throwable throwable) {
    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
    String logMessage = "[" + timestamp + "] "
        + "[" + level + "] "
        + message;

    // Determine the output stream
    java.io.PrintStream outStream = (level == LogLevel.ERROR || level == LogLevel.WARNING) ? System.err : System.out;

    outStream.println(logMessage);

    if (throwable != null) {
      // Print stack trace to the same stream
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      throwable.printStackTrace(pw);
      outStream.print(sw.toString()); // Use print to avoid an extra newline after the stack trace
      pw.close();
    }
  }

  /**
   * Logs an informational message.
   *
   * @param message The message to log.
   */
  public static void info(String message) {
    log(LogLevel.INFO, message, null);
  }

  /**
   * Logs a debug message.
   *
   * @param message The message to log.
   */
  public static void debug(String message) {
    log(LogLevel.DEBUG, message, null);
  }

  /**
   * Logs a warning message.
   *
   * @param message The message to log.
   */
  public static void warning(String message) {
    log(LogLevel.WARNING, message, null);
  }

  /**
   * Logs a warning message with an associated throwable (e.g., an exception).
   * The stack trace of the throwable will be printed.
   *
   * @param message   The message to log.
   * @param throwable The throwable (e.g., exception) to include.
   */
  public static void warning(String message, Throwable throwable) {
    log(LogLevel.WARNING, message, throwable);
  }

  /**
   * Logs an error message.
   *
   * @param message The message to log.
   */
  public static void error(String message) {
    log(LogLevel.ERROR, message, null);
  }

  /**
   * Logs an error message with an associated throwable (e.g., an exception).
   * The stack trace of the throwable will be printed.
   *
   * @param message   The message to log.
   * @param throwable The throwable (e.g., exception) to include.
   */
  public static void error(String message, Throwable throwable) {
    log(LogLevel.ERROR, message, throwable);
  }
}