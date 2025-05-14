package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class VBoxController {

    // TODO 14: Declare personData List and currentIndex
    private List<String[]> personData = new ArrayList<>();
    private int currentIndex = 0;

    // TODO 9: @FXML annotations
    @FXML
    private Label nameLabel;

    @FXML
    private Label cityLabel;

    @FXML
    private Label zipcodeLabel;

    @FXML
    private Button nextButton;

    @FXML
    private Button prevButton;

    // TODO 21: initialize() method
    @FXML
    public void initialize() {
        // TODO 22: Fetch data
        personData = fetchPersonData();
        if (!personData.isEmpty()) {
            // TODO 23: Display the first record
            displayRecord(currentIndex);
        }
    }

    // TODO 15: Method to fetch data from database
    private List<String[]> fetchPersonData() {
        List<String[]> data = new ArrayList<>();
        String query = "SELECT name, city, zipcode FROM person"; // TODO 16

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                // TODO 17: Extract columns
                String name = resultSet.getString("name");
                String city = resultSet.getString("city");
                String zipcode = resultSet.getString("zipcode");

                data.add(new String[]{name, city, zipcode});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    // TODO 18: displayRecord() method
    private void displayRecord(int index) {
        if (index >= 0 && index < personData.size()) {
            String[] person = personData.get(index);
            // TODO 19 & 20: Set label texts
            nameLabel.setText("Name: " + person[0]);
            cityLabel.setText("City: " + person[1]);
            zipcodeLabel.setText("Zipcode: " + person[2]);
        }
    }

    // TODO 24: Next button event
    @FXML
    void nextRecord(ActionEvent event) {
        if (currentIndex < personData.size() - 1) {
            currentIndex++;
            displayRecord(currentIndex);
        }
    }

    // Previous button event
    @FXML
    void prevRecord(ActionEvent event) {
        if (currentIndex > 0) {
            currentIndex--;
            displayRecord(currentIndex);
        }
    }

    // TODO 25: Switch scene method
    @FXML
    void switchHScene(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("hboxscene.fxml"));
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}