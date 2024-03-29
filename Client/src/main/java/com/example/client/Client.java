package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Client extends Application {

    private final FXMLLoader loader = new FXMLLoader();

    @Override
    public void start(Stage stage) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("client.fxml")) {
            Parent root = loader.load(inputStream);
            stage.setTitle("Cloud storage");
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

