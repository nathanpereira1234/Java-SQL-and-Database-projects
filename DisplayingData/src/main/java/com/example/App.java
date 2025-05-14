package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
        scene = new Scene(loadFXML("vboxscene"), 320, 240);
        stage.setScene(scene);
        stage.setTitle("Person Viewer");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        try {
            // TODO 13: Check DB connection before launching app
            Connection connection = Database.getConnection();
            if (connection != null && !connection.isClosed()) {
                System.out.println("Database connected");
            }
        } catch (SQLException e) {
            System.out.println("Database not connected");
            e.printStackTrace();
        }

        launch(); // Start JavaFX application
    }
}
