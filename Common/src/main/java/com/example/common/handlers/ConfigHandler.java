package com.example.common.handlers;

import lombok.AllArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigHandler {

    @AllArgsConstructor
    public enum Name {
        Client ("Client"),
        Server ("Server");

        private final String title;
    }

    public static Properties handleConfig(Name name) {
        String configFilePath = name.title + "/src/main/resources/configuration.properties";
        Properties prop = new Properties();
        try (FileInputStream propsInput = new FileInputStream(configFilePath)) {
            prop.load(propsInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}
