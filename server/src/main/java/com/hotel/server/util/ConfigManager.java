package com.hotel.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties props;

    private ConfigManager(String configFile) {
        props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config: " + e.getMessage());
        }
    }

    public static void init(String configFile) {
        if (instance == null) instance = new ConfigManager(configFile);
    }

    public static ConfigManager getInstance() {
        if (instance == null) throw new IllegalStateException("ConfigManager not initialized");
        return instance;
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public int getServerPort() {
        return Integer.parseInt(props.getProperty("server.port", "5555"));
    }
}
