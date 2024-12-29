package BetterPipes;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ConfigManager {
    private final Path configFilePath;
    private final Properties properties;

    // Constructor: Initialize config file path and load properties
    public ConfigManager(Path configDir, String filename) throws IOException {
        this.configFilePath = configDir.resolve(filename);
        this.properties = new Properties();

        if (Files.exists(configFilePath)) {
            // Load existing properties
            try (BufferedReader reader = Files.newBufferedReader(configFilePath)) {
                properties.load(reader);
            }
        } else {
            // Create a new file with default values if it doesn't exist
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            generateDefaultConfig();
            saveConfig();
        }
    }

    // Method to get a value with a default fallback
    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // Overloaded method to get an integer value with a default fallback
    public int getInt(String key, int defaultValue) {
        int r = Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        System.out.println("load "+key+" = "+r);
        return r;
    }

    // Overloaded method to get a double value with a default fallback
    public double getDouble(String key, double defaultValue) {
        return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    // Method to set a value
    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    // Overloaded method to set an integer value
    public void set(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    // Overloaded method to set a double value
    public void set(String key, double value) {
        properties.setProperty(key, String.valueOf(value));
    }

    // Save the properties to the configuration file
    public void saveConfig() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(configFilePath)) {
            properties.store(writer, "Configuration File");
        }
    }

    // Generate default configuration values
    private void generateDefaultConfig() {
        // Add your default values here
        setDefault("MAX_OUTPUT_RATE", "40");
        setDefault("MAIN_REQUIRED_FILL_FOR_MAX_OUTPUT", "200");
        setDefault("MAIN_CAPACITY", "400");

        setDefault("CONNECTION_REQUIRED_FILL_FOR_MAX_OUTPUT", "100");
        setDefault("CONNECTION_CAPACITY", "200");

        setDefault("Z_STATE_UPDATE_TICKS", "20");
        setDefault("Z_FORCE_OUTPUT_AFTER_TICKS", "10");
    }

    // Method to set a default value if the key doesn't exist
    public void setDefault(String key, String defaultValue) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, defaultValue);
        }
    }
}
