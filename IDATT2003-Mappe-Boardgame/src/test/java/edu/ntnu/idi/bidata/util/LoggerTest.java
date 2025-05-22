package edu.ntnu.idi.bidata.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        outContent.reset();
        errContent.reset();
    }



    @Test
    @DisplayName("info() should log message to stdout")
    void testInfoLogsToStdout() {
        String message = "This is an info message";
        Logger.info(message);

        assertTrue(outContent.toString().contains("[INFO] " + message));
        assertEquals(0, errContent.toString().length());
    }

    @Test
    @DisplayName("debug() should log message to stdout")
    void testDebugLogsToStdout() {
        String message = "This is a debug message";
        Logger.debug(message);

        assertTrue(outContent.toString().contains("[DEBUG] " + message));
        assertEquals(0, errContent.toString().length());
    }

    @Test
    @DisplayName("warning() should log message to stderr")
    void testWarningLogsToStderr() {
        String message = "This is a warning message";
        Logger.warning(message);

        assertTrue(errContent.toString().contains("[WARNING] " + message));
        assertEquals(0, outContent.toString().length());
    }

    @Test
    @DisplayName("warning() with throwable should log message and stack trace to stderr")
    void testWarningWithThrowableLogsToStderr() {
        String message = "This is a warning message with exception";
        Exception exception = new RuntimeException("Test exception");

        Logger.warning(message, exception);

        String output = errContent.toString();
        assertTrue(output.contains("[WARNING] " + message));
        assertTrue(output.contains("java.lang.RuntimeException: Test exception"));
        assertTrue(output.contains("at " + getClass().getName()));
    }

    @Test
    @DisplayName("error() should log message to stderr")
    void testErrorLogsToStderr() {
        String message = "This is an error message";
        Logger.error(message);

        assertTrue(errContent.toString().contains("[ERROR] " + message));
        assertEquals(0, outContent.toString().length());
    }

    @Test
    @DisplayName("error() with throwable should log message and stack trace to stderr")
    void testErrorWithThrowableLogsToStderr() {
        String message = "This is an error message with exception";
        Exception exception = new RuntimeException("Test exception");

        Logger.error(message, exception);

        String output = errContent.toString();
        assertTrue(output.contains("[ERROR] " + message));
        assertTrue(output.contains("java.lang.RuntimeException: Test exception"));
        assertTrue(output.contains("at " + getClass().getName()));
    }

    @Test
    @DisplayName("Logger should include timestamp")
    void testLoggerIncludesTimestamp() {
        Logger.info("Test");
        String output = outContent.toString();

        assertTrue(output.contains("[INFO] Test"));
    }
}