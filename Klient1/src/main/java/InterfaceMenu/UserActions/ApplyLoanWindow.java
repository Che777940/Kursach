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

public class ApplyLoanWindow {
    private final Stage stage;

    public ApplyLoanWindow() {
        stage = new Stage();
        stage.setTitle("Подача заявки на кредит");

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

        Label loanAmountLabel = new Label("Сумма кредита:");
        loanAmountLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        grid.add(loanAmountLabel, 0, 1);

        TextField loanAmountField = new TextField();
        loanAmountField.setPromptText("Введите сумму кредита");
        loanAmountField.setStyle(
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
        grid.add(loanAmountField, 1, 1);

        Label loanTermLabel = new Label("Срок кредита (месяцы):");
        loanTermLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        grid.add(loanTermLabel, 0, 2);

        TextField loanTermField = new TextField();
        loanTermField.setPromptText("Введите срок кредита");
        loanTermField.setStyle(
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
        grid.add(loanTermField, 1, 2);

        Label purposeLabel = new Label("Цель кредита:");
        purposeLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        grid.add(purposeLabel, 0, 3);

        ComboBox<String> purposeComboBox = new ComboBox<>();
        purposeComboBox.getItems().addAll("Ипотека", "Автокредит", "Потребительский", "Образование");
        purposeComboBox.setValue("Ипотека");
        purposeComboBox.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-max-width: 300px;"
        );
        grid.add(purposeComboBox, 1, 3);

        Button submitButton = new Button("Подать заявку");
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

        submitButton.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);

        submitButton.setOnMouseEntered(e -> submitButton.setStyle(buttonStyle + buttonHoverStyle));
        submitButton.setOnMouseExited(e -> submitButton.setStyle(buttonStyle));
        backButton.setOnMouseEntered(e -> backButton.setStyle(buttonStyle + buttonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(buttonStyle));

        grid.add(submitButton, 1, 4);
        grid.add(backButton, 1, 5);

        submitButton.setOnAction(e -> {
            try {
                String clientIdStr = clientIdField.getText().trim();
                String loanAmountStr = loanAmountField.getText().trim().replace(',', '.');
                String loanTermStr = loanTermField.getText().trim();
                String purpose = purposeComboBox.getValue().toLowerCase();

                if (clientIdStr.isEmpty() || loanAmountStr.isEmpty() || loanTermStr.isEmpty()) {
                    showAlert("Ошибка", "Все поля должны быть заполнены!");
                    return;
                }

                int clientId = Integer.parseInt(clientIdStr);
                double loanAmount = Double.parseDouble(loanAmountStr);
                int loanTerm = Integer.parseInt(loanTermStr);

                if (loanAmount <= 0 || loanTerm <= 0) {
                    showAlert("Ошибка", "Сумма и срок кредита должны быть положительными!");
                    return;
                }

                String request = String.format("ADD_CREDIT_APPLICATION %d %.2f %d %s", clientId, loanAmount, loanTerm, purpose);
                try (ServerConnection connection = new ServerConnection()) {
                    String response = connection.sendRequest(request);
                    if (response.startsWith("OK")) {
                        showAlert("Успех", "Заявка успешно подана!");
                        stage.close();
                        new InterfaceMenu.UserMain().start(new Stage());
                    } else {
                        showAlert("Ошибка", response);
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Неверный формат данных! Убедитесь, что ID, сумма и срок кредита — числа.");
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
        root.getChildren().add(grid);

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