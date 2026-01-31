package dorm.util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for CSV file operations.
 * Provides common functionality for reading and writing CSV files.
 */
public final class CsvHelper {
    
    private static final String DATA_DIR = "data";
    
    private CsvHelper() {}
    
    /**
     * Get the data directory path, creating it if it doesn't exist
     */
    public static Path getDataDirectory() {
        Path dataPath = Paths.get(DATA_DIR);
        if (!Files.exists(dataPath)) {
            try {
                Files.createDirectories(dataPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create data directory", e);
            }
        }
        return dataPath;
    }
    
    /**
     * Get the path to a specific CSV file
     */
    public static Path getCsvPath(String filename) {
        return getDataDirectory().resolve(filename);
    }
    
    /**
     * Read all lines from a CSV file (skipping header if present)
     */
    public static List<String[]> readAll(String filename, boolean hasHeader) {
        Path path = getCsvPath(filename);
        List<String[]> records = new ArrayList<>();
        
        if (!Files.exists(path)) {
            return records;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirst = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirst && hasHeader) {
                    isFirst = false;
                    continue;
                }
                isFirst = false;
                
                if (!line.trim().isEmpty()) {
                    records.add(parseCsvLine(line));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + filename, e);
        }
        
        return records;
    }
    
    /**
     * Write all records to a CSV file
     */
    public static void writeAll(String filename, String header, List<String[]> records) {
        Path path = getCsvPath(filename);
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            if (header != null) {
                writer.write(header);
                writer.newLine();
            }
            
            for (String[] record : records) {
                writer.write(toCsvLine(record));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing CSV file: " + filename, e);
        }
    }
    
    /**
     * Append a single record to a CSV file (creates file with header if doesn't exist)
     */
    public static void append(String filename, String header, String[] record) {
        Path path = getCsvPath(filename);
        
        try {
            boolean fileExists = Files.exists(path);
            
            try (BufferedWriter writer = Files.newBufferedWriter(path, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                
                if (!fileExists && header != null) {
                    writer.write(header);
                    writer.newLine();
                }
                
                writer.write(toCsvLine(record));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error appending to CSV file: " + filename, e);
        }
    }
    
    /**
     * Parse a CSV line into an array of values
     * Handles quoted values with embedded commas and escaped quotes
     */
    public static String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (inQuotes) {
                if (c == '"') {
                    // Check for escaped quote
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    values.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        values.add(current.toString());
        
        return values.toArray(new String[0]);
    }
    
    /**
     * Convert an array of values to a CSV line
     * Properly escapes quotes and wraps values containing commas or quotes
     */
    public static String toCsvLine(String[] values) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escapeCsvValue(values[i]));
        }
        
        return sb.toString();
    }
    
    /**
     * Escape a single CSV value
     */
    public static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Null-safe string value
     */
    public static String nullSafe(String value) {
        return value == null ? "" : value;
    }
    
    /**
     * Convert empty string to null
     */
    public static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
