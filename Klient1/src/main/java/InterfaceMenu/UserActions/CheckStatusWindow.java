package InterfaceMenu.UserActions;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class CheckStatusWindow {
    private final Stage stage;

    public CheckStatusWindow() {
        stage = new Stage();
        stage.setTitle("Проверка статуса заявки");

        // Установка иконки окна
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/bank_icon.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        stage.setMaximized(true);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label clientIdLabel = new Label("ID клиента:");
        clientIdLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        grid.add(clientIdLabel, 0, 0);

        TextField clientIdField = new TextField();
        clientIdField.setPromptText("Введите ID клиента");
        clientIdField.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 10px; " +
                        "-fx-max-width: 300px;"
        );
        grid.add(clientIdField, 1, 0);

        Button checkButton = new Button("Проверить статус");
        Button backButton = new Button("Назад");

        // Стилизация кнопок
        String buttonStyle =
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 12px 30px; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);";

        String buttonHoverStyle =
                "-fx-background-color: #e6f0ff; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);";

        checkButton.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);

        checkButton.setOnMouseEntered(e -> checkButton.setStyle(buttonStyle + buttonHoverStyle));
        checkButton.setOnMouseExited(e -> checkButton.setStyle(buttonStyle));
        backButton.setOnMouseEntered(e -> backButton.setStyle(buttonStyle + buttonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(buttonStyle));

        grid.add(checkButton, 1, 1);
        grid.add(backButton, 1, 2);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefHeight(300);
        resultArea.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 10px; " +
                        "-fx-max-width: 600px;"
        );

        VBox layout = new VBox(20);
        layout.getChildren().addAll(grid, resultArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        checkButton.setOnAction(e -> {
            try {
                String clientIdStr = clientIdField.getText().trim();
                if (clientIdStr.isEmpty()) {
                    showAlert("Ошибка", "Введите ID клиента!");
                    return;
                }

                int clientId = Integer.parseInt(clientIdStr);
                String request = "GET_CREDIT_APPLICATIONS " + clientId;
                try (ServerConnection connection = new ServerConnection()) {
                    String response = connection.sendRequest(request);
                    if (response.startsWith("OK")) {
                        String[] parts = response.split("\\|");
                        StringBuilder result = new StringBuilder();
                        for (int i = 1; i < parts.length; i++) {
                            String[] appDetails = parts[i].split(",");
                            result.append(String.format("Заявка ID: %s, Клиент ID: %s, Сумма: %s, Срок: %s мес, Цель: %s, Статус: %s, Дата подачи: %s%n",
                                    appDetails[0], appDetails[1], appDetails[2], appDetails[3], appDetails[4], appDetails[5], appDetails[6]));
                        }
                        resultArea.setText(result.toString());
                    } else {
                        resultArea.setText(response);
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Неверный формат ID клиента! Убедитесь, что это число.");
            } catch (IOException ex) {
                showAlert("Ошибка", "Не удалось подключиться к серверу: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> {
            stage.close();
            new InterfaceMenu.UserMain().start(new Stage());
        });

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(layout);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}