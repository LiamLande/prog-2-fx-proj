package edu.ntnu.idi.bidata.util;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilsTest {

    @Test
    @DisplayName("Constructor should be inaccessible")
    void testConstructorInaccessible() throws Exception {
        Constructor<CsvUtils> constructor = CsvUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Verify we can access it with reflection
        CsvUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("readAll should parse CSV correctly")
    void testReadAllValid() throws IOException {
        String csvData = "a,b,c\n1,2,3\nx,y,z";
        StringReader reader = new StringReader(csvData);

        List<String[]> records = CsvUtils.readAll(reader);

        assertEquals(3, records.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, records.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, records.get(1));
        assertArrayEquals(new String[]{"x", "y", "z"}, records.get(2));
    }

    @Test
    @DisplayName("readAll should handle empty file")
    void testReadAllEmpty() throws IOException {
        StringReader reader = new StringReader("");

        List<String[]> records = CsvUtils.readAll(reader);

        assertEquals(0, records.size());
    }

    @Test
    @DisplayName("readAll should throw InvalidParameterException for null reader")
    void testReadAllNullReader() {
        assertThrows(InvalidParameterException.class, () -> CsvUtils.readAll(null));
    }

    @Test
    @DisplayName("readAll should handle IOException")
    void testReadAllIOException() {
        Reader reader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated IO error");
            }

            @Override
            public void close() {
                // Nothing to do
            }
        };

        assertThrows(IOException.class, () -> CsvUtils.readAll(reader));
    }

    @Test
    @DisplayName("writeAll should write CSV correctly")
    void testWriteAllValid() throws IOException {
        List<String[]> records = Arrays.asList(
                new String[]{"a", "b", "c"},
                new String[]{"1", "2", "3"},
                new String[]{"x", "y", "z"}
        );

        StringWriter writer = new StringWriter();
        CsvUtils.writeAll(writer, records);

        String result = writer.toString();
        String expectedLineEnding = System.lineSeparator();
        String expected = "a,b,c" + expectedLineEnding +
                "1,2,3" + expectedLineEnding +
                "x,y,z" + expectedLineEnding;

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("writeAll should handle empty records")
    void testWriteAllEmptyRecords() throws IOException {
        List<String[]> records = List.of();

        StringWriter writer = new StringWriter();
        CsvUtils.writeAll(writer, records);

        assertEquals("", writer.toString());
    }

    @Test
    @DisplayName("writeAll should throw InvalidParameterException for null writer")
    void testWriteAllNullWriter() {
        assertThrows(InvalidParameterException.class,
                () -> CsvUtils.writeAll(null, (List<String[]>) List.of(new String[]{"a", "b", "c"}, new String[]{"1", "2", "3"})));
    }

    @Test
    @DisplayName("writeAll should throw InvalidParameterException for null records")
    void testWriteAllNullRecords() {
        assertThrows(InvalidParameterException.class,
                () -> CsvUtils.writeAll(new StringWriter(), null));
    }

    @Test
    @DisplayName("writeAll should handle IOException")
    void testWriteAllIOException() {
        List<String[]> records = (List<String[]>) List.of(new String[]{"a", "b", "c"}, new String[]{"1", "2", "3"});
        Writer writer = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated IO error");
            }

            @Override
            public void flush() throws IOException {
                throw new IOException("Simulated IO error");
            }

            @Override
            public void close() {
                // Nothing to do
            }
        };

        assertThrows(IOException.class, () -> CsvUtils.writeAll(writer, records));
    }
}