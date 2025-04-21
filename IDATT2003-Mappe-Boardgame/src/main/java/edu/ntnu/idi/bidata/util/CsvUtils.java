package edu.ntnu.idi.bidata.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import edu.ntnu.idi.bidata.exception.InvalidParameterException;

/**
 * Utility methods for reading and writing CSV files.
 */
public final class CsvUtils {
  private CsvUtils() { /* prevent instantiation */ }

  /**
   * Reads all CSV records from the given Reader.
   * @param reader source of CSV data
   * @return list of records, each as array of values
   * @throws IOException if an I/O error occurs
   */
  public static List<String[]> readAll(Reader reader) throws IOException {
    if (reader == null) throw new InvalidParameterException("Reader must not be null");
    List<String[]> records = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(reader)) {
      String line;
      while ((line = br.readLine()) != null) {
        // Simple split on comma; does not handle quotes
        records.add(line.split(","));
      }
    }
    return records;
  }

  /**
   * Writes all records to the given Writer as CSV.
   * @param writer destination for CSV data
   * @param records list of records to write
   * @throws IOException if an I/O error occurs
   */
  public static void writeAll(Writer writer, List<String[]> records) throws IOException {
    if (writer == null) throw new InvalidParameterException("Writer must not be null");
    if (records == null) throw new InvalidParameterException("Records must not be null");
    for (String[] row : records) {
      writer.write(String.join(",", row));
      writer.write(System.lineSeparator());
    }
    writer.flush();
  }
}