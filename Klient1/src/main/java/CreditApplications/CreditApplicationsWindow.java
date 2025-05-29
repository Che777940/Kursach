package CreditApplications;

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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreditApplicationsWindow extends Application {
    private TableView<CreditApplication> applicationsTable;
    private ObservableList<CreditApplication> applicationsData;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Управление заявками на кредитование");

        // Открытие на полный экран
        stage.setMaximized(true);

        // Основной layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.CENTER);

        // Заголовок
        Label titleLabel = new Label("===== УПРАВЛЕНИЕ ЗАЯВКАМИ НА КРЕДИТОВАНИЕ =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        // Таблица заявок
        applicationsTable = new TableView<>();
        applicationsData = FXCollections.observableArrayList();
        applicationsTable.setItems(applicationsData);
        applicationsTable.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px;"
        );

        TableColumn<CreditApplication, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMinWidth(50);
        idColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<CreditApplication, Integer> clientIdColumn = new TableColumn<>("Клиент ID");
        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        clientIdColumn.setMinWidth(80);
        clientIdColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<CreditApplication, BigDecimal> loanAmountColumn = new TableColumn<>("Сумма кредита");
        loanAmountColumn.setCellValueFactory(new PropertyValueFactory<>("loanAmount"));
        loanAmountColumn.setMinWidth(100);
        loanAmountColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<CreditApplication, Integer> loanTermColumn = new TableColumn<>("Срок (месяцы)");
        loanTermColumn.setCellValueFactory(new PropertyValueFactory<>("loanTerm"));
        loanTermColumn.setMinWidth(100);
        loanTermColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<CreditApplication, String> purposeColumn = new TableColumn<>("Цель");
        purposeColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        purposeColumn.setMinWidth(100);
        purposeColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<CreditApplication, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setMinWidth(100);
        statusColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<CreditApplication, LocalDate> submissionDateColumn = new TableColumn<>("Дата подачи");
        submissionDateColumn.setCellValueFactory(new PropertyValueFactory<>("submissionDate"));
        submissionDateColumn.setMinWidth(100);
        submissionDateColumn.setStyle("-fx-alignment: CENTER;");

        applicationsTable.getColumns().addAll(idColumn, clientIdColumn, loanAmountColumn, loanTermColumn, purposeColumn, statusColumn, submissionDateColumn);

        // Кнопки
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button approveButton = createStyledButton("Одобрить", e -> changeApplicationStatus("APPROVED", stage));
        Button rejectButton = createStyledButton("Отклонить", e -> changeApplicationStatus("REJECTED", stage));
        Button refreshButton = createStyledButton("Обновить", e -> loadApplications());
        Button backButton = createStyledButton("Назад", e -> {
            stage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

        buttonBox.getChildren().addAll(approveButton, rejectButton, refreshButton, backButton);

        // Сборка layout
        mainLayout.getChildren().addAll(titleLabel, applicationsTable, buttonBox);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        loadApplications();
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

    private void loadApplications() {
        applicationsData.clear();
        try (ServerConnection connection = new ServerConnection()) {
            String response = connection.sendRequest("GET_ALL_CREDIT_APPLICATIONS");
            System.out.println("Ответ сервера: " + response);
            if (response.startsWith("OK")) {
                String[] parts = response.split("\\|");
                System.out.println("Количество записей: " + (parts.length - 1));
                if (parts.length <= 1) {
                    showAlert("Информация", "Заявки отсутствуют.");
                    return;
                }
                for (int i = 1; i < parts.length; i++) {
                    System.out.println("Обработка записи: " + parts[i]);
                    String[] appDetails = parts[i].split(",");
                    if (appDetails.length != 7) {
                        System.err.println("Некорректный формат данных заявки: " + parts[i]);
                        continue;
                    }
                    try {
                        String status = appDetails[5];
                        // Показываем только заявки со статусом PENDING
                        if (!status.equals("PENDING")) {
                            System.out.println("Пропущена заявка с ID " + appDetails[0] + ", статус: " + status);
                            continue;
                        }
                        CreditApplication app = new CreditApplication(
                                Integer.parseInt(appDetails[0]),
                                Integer.parseInt(appDetails[1]),
                                new BigDecimal(appDetails[2]),
                                Integer.parseInt(appDetails[3]),
                                appDetails[4],
                                appDetails[5],
                                LocalDate.parse(appDetails[6])
                        );
                        applicationsData.add(app);
                        System.out.println("Добавлена заявка с ID " + appDetails[0] + ", статус: " + appDetails[5]);
                    } catch (Exception e) {
                        System.err.println("Ошибка парсинга заявки: " + parts[i] + " | Ошибка: " + e.getMessage());
                    }
                }
                if (applicationsData.isEmpty()) {
                    showAlert("Информация", "Нет заявок со статусом PENDING.");
                }
            } else {
                showAlert("Ошибка", response);
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    private void changeApplicationStatus(String status, Stage stage) {
        CreditApplication selectedApplication = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApplication == null) {
            showAlert("Ошибка", "Выберите заявку!");
            return;
        }

        try (ServerConnection connection = new ServerConnection()) {
            String request = String.format("UPDATE_CREDIT_APPLICATION_STATUS %d %s", selectedApplication.getId(), status);
            String response = connection.sendRequest(request);
            if (response.startsWith("OK")) {
                showAlert("Успех", "Статус заявки обновлен!");
                stage.close();
                new InterfaceMenu.AdminMain().start(new Stage());
            } else {
                showAlert("Ошибка", response);
            }
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