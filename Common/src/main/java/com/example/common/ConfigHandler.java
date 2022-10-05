package com.example.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigHandler {

    public static Properties handleConfig() {
        String configFilePath = "src/main/resources/configuration.properties";
        Properties prop = new Properties();
        try (FileInputStream propsInput = new FileInputStream(configFilePath)) {
            prop.load(propsInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}
