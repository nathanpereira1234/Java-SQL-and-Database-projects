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

public class HBoxController {

    // TODO 26: @FXML annotations from hboxscene.fxml
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

    private List<String[]> personData = new ArrayList<>();  // List to store fetched records
    private int currentIndex = 0;

    // TODO 27: Copy initialize(), fetchPersonData(), displayRecord(), nextRecord()

    @FXML
    public void initialize() {
        personData = fetchPersonData();
        if (!personData.isEmpty()) {
            displayRecord(currentIndex);
        }
    }

    private List<String[]> fetchPersonData() {
        List<String[]> data = new ArrayList<>();
        String query = "SELECT name, city, zipcode FROM person";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
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

    private void displayRecord(int index) {
        if (index >= 0 && index < personData.size()) {
            String[] person = personData.get(index);
            nameLabel.setText("Name: " + person[0]);
            cityLabel.setText("City: " + person[1]);
            zipcodeLabel.setText("Zipcode: " + person[2]);
        }
    }

    @FXML
    void nextRecord(ActionEvent event) {
        if (currentIndex < personData.size() - 1) {
            currentIndex++;
            displayRecord(currentIndex);
        }
    }

    @FXML
    void prevRecord(ActionEvent event) {
        if (currentIndex > 0) {
            currentIndex--;
            displayRecord(currentIndex);
        }
    }

    // TODO 28: switch to gridscene.fxml
    @FXML
    void switchGridScene(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("gridscene.fxml"));
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}

