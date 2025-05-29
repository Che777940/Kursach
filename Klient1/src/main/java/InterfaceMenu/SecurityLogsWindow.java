package InterfaceMenu;

import InterfaceMenu.Models.SecurityLog;
import InterfaceMenu.UserActions.ServerConnection;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.sql.Timestamp;

public class SecurityLogsWindow extends Application {
    private TableView<SecurityLog> logsTable;
    private ObservableList<SecurityLog> logsData;
    private TextField userFilterField;
    private TextField actionFilterField;
    private TextField daysField;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Управление логами безопасности");

        // Установка иконки окна
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        stage.setMaximized(true);

        // Основной layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.CENTER);

        // Заголовок
        Label titleLabel = new Label("===== УПРАВЛЕНИЕ ЛОГАМИ БЕЗОПАСНОСТИ =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        // Фильтры
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER);

        Label userFilterLabel = new Label("Фильтр по пользователю:");
        userFilterLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-text-fill: #ffffff;");
        userFilterField = new TextField();
        userFilterField.setPromptText("Введите имя пользователя");
        userFilterField.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 8px;"
        );

        Label actionFilterLabel = new Label("Фильтр по действию:");
        actionFilterLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-text-fill: #ffffff;");
        actionFilterField = new TextField();
        actionFilterField.setPromptText("Введите действие");
        actionFilterField.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 8px;"
        );

        filterBox.getChildren().addAll(userFilterLabel, userFilterField, actionFilterLabel, actionFilterField);

        // Поле для ввода количества дней
        HBox daysBox = new HBox(10);
        daysBox.setAlignment(Pos.CENTER);

        Label daysLabel = new Label("Очистить логи старше (дней):");
        daysLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-text-fill: #ffffff;");
        daysField = new TextField();
        daysField.setPromptText("Введите количество дней");
        daysField.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 8px; " +
                        "-fx-max-width: 150px;"
        );

        daysBox.getChildren().addAll(daysLabel, daysField);

        // Таблица логов
        logsTable = new TableView<>();
        logsData = FXCollections.observableArrayList();
        logsTable.setItems(logsData);
        logsTable.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px;"
        );

        TableColumn<SecurityLog, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMinWidth(50);
        idColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<SecurityLog, String> usernameColumn = new TableColumn<>("Пользователь");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setMinWidth(150);
        usernameColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<SecurityLog, String> actionColumn = new TableColumn<>("Действие");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionColumn.setMinWidth(200);
        actionColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<SecurityLog, Timestamp> timestampColumn = new TableColumn<>("Время");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampColumn.setMinWidth(150);
        timestampColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<SecurityLog, String> detailsColumn = new TableColumn<>("Детали");
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsColumn.setMinWidth(300);
        detailsColumn.setStyle("-fx-alignment: CENTER;");

        logsTable.getColumns().addAll(idColumn, usernameColumn, actionColumn, timestampColumn, detailsColumn);

        // Кнопки
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button applyFilterButton = createStyledButton("Применить фильтры", e -> loadLogs());
        Button clearLogsButton = createStyledButton("Очистить старые логи", e -> clearOldLogs());
        Button refreshButton = createStyledButton("Обновить", e -> {
            userFilterField.clear();
            actionFilterField.clear();
            loadLogs();
        });
        Button backButton = createStyledButton("Назад", e -> {
            stage.close();
            new AdminMain().start(new Stage());
        });

        buttonBox.getChildren().addAll(applyFilterButton, clearLogsButton, refreshButton, backButton);

        // Сборка layout
        mainLayout.getChildren().addAll(titleLabel, filterBox, daysBox, logsTable, buttonBox);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        loadLogs();
    }

    private Button createStyledButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
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

        button.setStyle(buttonStyle);
        button.setOnMouseEntered(e -> button.setStyle(buttonStyle + buttonHoverStyle));
        button.setOnMouseExited(e -> button.setStyle(buttonStyle));
        button.setOnAction(handler);
        return button;
    }

    private void loadLogs() {
        logsData.clear();
        try (ServerConnection connection = new ServerConnection()) {
            StringBuilder request = new StringBuilder("GET_SECURITY_LOGS");
            String userFilter = userFilterField.getText().trim();
            String actionFilter = actionFilterField.getText().trim();

            if (!userFilter.isEmpty()) {
                request.append(" USER_FILTER ").append(userFilter);
            }
            if (!actionFilter.isEmpty()) {
                request.append(" ACTION_FILTER ").append(actionFilter);
            }

            String response = connection.sendRequest(request.toString());
            System.out.println("Ответ сервера: " + response);
            if (response.startsWith("OK")) {
                String[] parts = response.split("\\|");
                if (parts.length <= 1) {
                    showAlert("Информация", "Логи отсутствуют.");
                    return;
                }
                for (int i = 1; i < parts.length; i++) {
                    System.out.println("Обработка записи: " + parts[i]);
                    String[] logDetails = parts[i].split(",", 5);
                    if (logDetails.length != 5) {
                        System.err.println("Некорректный формат данных лога: " + parts[i]);
                        continue;
                    }
                    try {
                        SecurityLog log = new SecurityLog(
                                Integer.parseInt(logDetails[0]),
                                logDetails[1],
                                logDetails[2],
                                Timestamp.valueOf(logDetails[3]),
                                logDetails[4].isEmpty() ? null : logDetails[4]
                        );
                        logsData.add(log);
                        System.out.println("Добавлен лог с ID " + logDetails[0]);
                    } catch (Exception e) {
                        System.err.println("Ошибка парсинга лога: " + parts[i] + " | Ошибка: " + e.getMessage());
                    }
                }
                if (logsData.isEmpty()) {
                    showAlert("Информация", "Нет логов, соответствующих фильтрам.");
                }
            } else {
                showAlert("Ошибка", response);
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    private void clearOldLogs() {
        String daysText = daysField.getText().trim();
        if (daysText.isEmpty()) {
            showAlert("Ошибка", "Введите количество дней!");
            return;
        }

        try {
            int days = Integer.parseInt(daysText);
            if (days <= 0) {
                showAlert("Ошибка", "Количество дней должно быть положительным!");
                return;
            }
            try (ServerConnection connection = new ServerConnection()) {
                String request = String.format("CLEAR_OLD_LOGS %d", days);
                String response = connection.sendRequest(request);
                if (response.startsWith("OK")) {
                    showAlert("Успех", response.substring(4));
                    loadLogs();
                } else {
                    showAlert("Ошибка", response);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Неверный формат количества дней!");
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}