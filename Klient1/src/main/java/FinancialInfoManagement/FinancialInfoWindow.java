package FinancialInfoManagement;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FinancialInfoWindow extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    private ComboBox<ClientDTO> clientComboBox;
    private TextField creditBalanceField;
    private TextField overdueAmountField;
    private TextField openCreditsField;
    private TextField creditInquiriesField;
    private TextField creditLimitField;
    private TextField creditTypesField;
    private TextField firstCreditDateField;
    private Label financialInfoLabel;
    private Button addButton;
    private Button updateButton;
    private Button deleteButton;
    private Button refreshButton;

    private List<ClientDTO> clients = new ArrayList<>();
    private int currentFinancialInfoId = -1; // Для хранения ID текущей финансовой информации

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Управление финансовой информацией");

        // Установка иконки окна
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        primaryStage.setMaximized(true);

        // Основной layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.CENTER);

        // Заголовок
        Label titleLabel = new Label("===== УПРАВЛЕНИЕ ФИНАНСОВОЙ ИНФОРМАЦИЕЙ =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        // Выбор клиента
        Label clientLabel = new Label("Выберите клиента:");
        clientLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );

        clientComboBox = new ComboBox<>();
        clientComboBox.setPromptText("Выберите клиента");
        clientComboBox.setCellFactory(param -> new ListCell<ClientDTO>() {
            @Override
            protected void updateItem(ClientDTO client, boolean empty) {
                super.updateItem(client, empty);
                setText(empty || client == null ? null : client.toString());
            }
        });
        clientComboBox.setButtonCell(new ListCell<ClientDTO>() {
            @Override
            protected void updateItem(ClientDTO client, boolean empty) {
                super.updateItem(client, empty);
                setText(empty || client == null ? null : client.toString());
            }
        });
        clientComboBox.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-max-width: 400px;"
        );
        clientComboBox.setOnAction(e -> loadFinancialInfo());

        // Форма для финансовой информации
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        String fieldStyle =
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 10px; " +
                        "-fx-max-width: 400px;";

        Label creditBalanceLabel = new Label("Кредитный баланс:");
        creditBalanceLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        creditBalanceField = new TextField();
        creditBalanceField.setPromptText("0.00");
        creditBalanceField.setStyle(fieldStyle);

        Label overdueAmountLabel = new Label("Просроченная сумма:");
        overdueAmountLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        overdueAmountField = new TextField();
        overdueAmountField.setPromptText("0.00");
        overdueAmountField.setStyle(fieldStyle);

        Label openCreditsLabel = new Label("Открытые кредиты:");
        openCreditsLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        openCreditsField = new TextField();
        openCreditsField.setPromptText("0");
        openCreditsField.setStyle(fieldStyle);

        Label creditInquiriesLabel = new Label("Кредитные запросы:");
        creditInquiriesLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        creditInquiriesField = new TextField();
        creditInquiriesField.setPromptText("0");
        creditInquiriesField.setStyle(fieldStyle);

        Label creditLimitLabel = new Label("Кредитный лимит:");
        creditLimitLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        creditLimitField = new TextField();
        creditLimitField.setPromptText("0.00 или NULL");
        creditLimitField.setStyle(fieldStyle);

        Label creditTypesLabel = new Label("Типы кредитов:");
        creditTypesLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        creditTypesField = new TextField();
        creditTypesField.setPromptText("ипотека,кредитная карта или NULL");
        creditTypesField.setStyle(fieldStyle);

        Label firstCreditDateLabel = new Label("Дата первого кредита:");
        firstCreditDateLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        firstCreditDateField = new TextField();
        firstCreditDateField.setPromptText("ГГГГ-ММ-ДД или NULL");
        firstCreditDateField.setStyle(fieldStyle);

        formGrid.add(creditBalanceLabel, 0, 0);
        formGrid.add(creditBalanceField, 1, 0);
        formGrid.add(overdueAmountLabel, 0, 1);
        formGrid.add(overdueAmountField, 1, 1);
        formGrid.add(openCreditsLabel, 0, 2);
        formGrid.add(openCreditsField, 1, 2);
        formGrid.add(creditInquiriesLabel, 0, 3);
        formGrid.add(creditInquiriesField, 1, 3);
        formGrid.add(creditLimitLabel, 0, 4);
        formGrid.add(creditLimitField, 1, 4);
        formGrid.add(creditTypesLabel, 0, 5);
        formGrid.add(creditTypesField, 1, 5);
        formGrid.add(firstCreditDateLabel, 0, 6);
        formGrid.add(firstCreditDateField, 1, 6);

        // Отображение текущей финансовой информации
        financialInfoLabel = new Label("Финансовая информация не выбрана");
        financialInfoLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );

        // Кнопки
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        addButton = createStyledButton("Добавить", e -> addFinancialInfo());
        updateButton = createStyledButton("Обновить", e -> updateFinancialInfo());
        deleteButton = createStyledButton("Удалить", e -> deleteFinancialInfo());
        refreshButton = createStyledButton("Обновить список", e -> loadClients());
        Button backButton = createStyledButton("Назад", e -> {
            primaryStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

        // Изначально кнопки неактивны
        addButton.setDisable(true);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        buttonBox.getChildren().addAll(addButton, updateButton, deleteButton, refreshButton, backButton);

        // Сборка layout
        mainLayout.getChildren().addAll(
                titleLabel,
                clientLabel, clientComboBox,
                formGrid,
                financialInfoLabel,
                buttonBox
        );

        // Инициализация
        loadClients();

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
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

    // Загрузка списка клиентов
    private void loadClients() {
        clients.clear();
        clientComboBox.getItems().clear();
        financialInfoLabel.setText("Финансовая информация не выбрана");
        clearForm();
        addButton.setDisable(true);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_CLIENTS");
            String response = in.readLine();
            if (response != null && response.startsWith("OK")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] clientData = parts[i].split(":");
                    if (clientData.length == 2) {
                        int id = Integer.parseInt(clientData[0]);
                        String fullName = clientData[1];
                        clients.add(new ClientDTO(id, fullName));
                    }
                }
                clientComboBox.getItems().addAll(clients);
            } else {
                showAlert("Ошибка", response != null ? response : "Нет ответа от сервера");
            }
        } catch (Exception e) {
            showAlert("Ошибка соединения", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    // Загрузка финансовой информации для выбранного клиента
    private void loadFinancialInfo() {
        ClientDTO selectedClient = clientComboBox.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            financialInfoLabel.setText("Финансовая информация не выбрана");
            clearForm();
            addButton.setDisable(true);
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            return;
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_FINANCIAL_INFO " + selectedClient.getId());
            String response = in.readLine();
            if (response != null && response.startsWith("OK")) {
                String[] parts = response.split("\\|");
                if (parts.length == 11) {
                    currentFinancialInfoId = Integer.parseInt(parts[1]);
                    String creditBalance = parts[3].replace(',', '.');
                    String overdueAmount = parts[4].replace(',', '.');
                    creditBalanceField.setText(creditBalance);
                    overdueAmountField.setText(overdueAmount);
                    openCreditsField.setText(parts[5]);
                    creditInquiriesField.setText(parts[6]);
                    String creditLimit = parts[8].replace(',', '.');
                    creditLimitField.setText(creditLimit.equals("0.00") ? "" : creditLimit);
                    creditTypesField.setText(parts[9].isEmpty() ? "" : parts[9]);
                    firstCreditDateField.setText(parts[10].isEmpty() ? "" : parts[10]);
                    financialInfoLabel.setText("Финансовая информация для " + selectedClient.getFullName() +
                            " (ID: " + currentFinancialInfoId + ")");
                    // Финансовая информация существует: отключаем "Добавить", включаем остальные кнопки
                    addButton.setDisable(true);
                    updateButton.setDisable(false);
                    deleteButton.setDisable(false);
                }
            } else {
                currentFinancialInfoId = -1;
                clearForm();
                financialInfoLabel.setText("Финансовая информация для " + selectedClient.getFullName() + " не найдена");
                // Финансовая информация не существует: включаем "Добавить", отключаем остальные кнопки
                addButton.setDisable(false);
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
                showAlert("Информация", response != null ? response : "Нет финансовой информации");
            }
        } catch (Exception e) {
            showAlert("Ошибка соединения", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    // Добавление финансовой информации
    private void addFinancialInfo() {
        ClientDTO selectedClient = clientComboBox.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Ошибка", "Выберите клиента");
            return;
        }

        try {
            // Заменяем запятые на точки в полях ввода
            String creditBalanceStr = creditBalanceField.getText().trim().replace(',', '.');
            String overdueAmountStr = overdueAmountField.getText().trim().replace(',', '.');
            String openCreditsStr = openCreditsField.getText().trim();
            String creditInquiriesStr = creditInquiriesField.getText().trim();
            String creditLimitStr = creditLimitField.getText().trim().replace(',', '.');
            String creditTypesStr = creditTypesField.getText().trim();
            String firstCreditDateStr = firstCreditDateField.getText().trim();

            // Валидация обязательных полей
            if (creditBalanceStr.isEmpty() || overdueAmountStr.isEmpty() ||
                    openCreditsStr.isEmpty() || creditInquiriesStr.isEmpty()) {
                showAlert("Ошибка", "Поля 'Кредитный баланс', 'Просроченная сумма', 'Открытые кредиты' и 'Кредитные запросы' должны быть заполнены");
                return;
            }

            BigDecimal creditBalance = new BigDecimal(creditBalanceStr);
            BigDecimal overdueAmount = new BigDecimal(overdueAmountStr);
            int openCredits = Integer.parseInt(openCreditsStr);
            int creditInquiries = Integer.parseInt(creditInquiriesStr);
            String creditLimitValue = creditLimitStr.isEmpty() ? "NULL" : creditLimitStr;
            String creditTypesValue = creditTypesStr.isEmpty() ? "NULL" : creditTypesStr;
            String firstCreditDateValue = firstCreditDateStr.isEmpty() ? "NULL" : firstCreditDateStr;

            // Валидация формата даты, если поле не пустое
            if (!firstCreditDateValue.equals("NULL")) {
                try {
                    LocalDate.parse(firstCreditDateValue);
                } catch (Exception e) {
                    showAlert("Ошибка", "Неверный формат даты в поле 'Дата первого кредита'. Используйте ГГГГ-ММ-ДД (например, 2020-01-01)");
                    return;
                }
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String command = String.format(Locale.US, "ADD_FINANCIAL_INFO %d %.2f %.2f %d %d %s %s %s",
                        selectedClient.getId(), creditBalance, overdueAmount, openCredits, creditInquiries,
                        creditLimitValue, creditTypesValue, firstCreditDateValue);
                out.println(command);
                String response = in.readLine();
                if (response != null && response.startsWith("OK")) {
                    showAlert("Успех", "Финансовая информация добавлена");
                    Stage stage = (Stage) addButton.getScene().getWindow();
                    stage.close();
                    new InterfaceMenu.AdminMain().start(new Stage());
                } else {
                    showAlert("Ошибка", response != null ? response : "Не удалось добавить финансовую информацию");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректные числовые значения (используйте точку для десятичных чисел, например, 120.00)");
        } catch (Exception e) {
            showAlert("Ошибка соединения", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    // Обновление финансовой информации
    private void updateFinancialInfo() {
        if (currentFinancialInfoId == -1) {
            showAlert("Ошибка", "Нет финансовой информации для обновления. Сначала выберите клиента с существующей информацией");
            return;
        }

        try {
            // Заменяем запятые на точки в полях ввода
            String creditBalanceStr = creditBalanceField.getText().trim().replace(',', '.');
            String overdueAmountStr = overdueAmountField.getText().trim().replace(',', '.');
            String openCreditsStr = openCreditsField.getText().trim();
            String creditInquiriesStr = creditInquiriesField.getText().trim();
            String creditLimitStr = creditLimitField.getText().trim().replace(',', '.');
            String creditTypesStr = creditTypesField.getText().trim();
            String firstCreditDateStr = firstCreditDateField.getText().trim();

            // Валидация обязательных полей
            if (creditBalanceStr.isEmpty() || overdueAmountStr.isEmpty() ||
                    openCreditsStr.isEmpty() || creditInquiriesStr.isEmpty()) {
                showAlert("Ошибка", "Поля 'Кредитный баланс', 'Просроченная сумма', 'Открытые кредиты' и 'Кредитные запросы' должны быть заполнены");
                return;
            }

            BigDecimal creditBalance = new BigDecimal(creditBalanceStr);
            BigDecimal overdueAmount = new BigDecimal(overdueAmountStr);
            int openCredits = Integer.parseInt(openCreditsStr);
            int creditInquiries = Integer.parseInt(creditInquiriesStr);
            String creditLimitValue = creditLimitStr.isEmpty() ? "NULL" : creditLimitStr;
            String creditTypesValue = creditTypesStr.isEmpty() ? "NULL" : creditTypesStr;
            String firstCreditDateValue = firstCreditDateStr.isEmpty() ? "NULL" : firstCreditDateStr;

            // Валидация формата даты, если поле не пустое
            if (!firstCreditDateValue.equals("NULL")) {
                try {
                    LocalDate.parse(firstCreditDateValue);
                } catch (Exception e) {
                    showAlert("Ошибка", "Неверный формат даты в поле 'Дата первого кредита'. Используйте ГГГГ-ММ-ДД (например, 2020-01-01)");
                    return;
                }
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String command = String.format(Locale.US, "UPDATE_FINANCIAL_INFO %d %.2f %.2f %d %d %s %s %s",
                        currentFinancialInfoId, creditBalance, overdueAmount, openCredits, creditInquiries,
                        creditLimitValue, creditTypesValue, firstCreditDateValue);
                out.println(command);
                String response = in.readLine();
                if (response != null && response.startsWith("OK")) {
                    showAlert("Успех", "Финансовая информация обновлена");
                    Stage stage = (Stage) updateButton.getScene().getWindow();
                    stage.close();
                    new InterfaceMenu.AdminMain().start(new Stage());
                } else {
                    showAlert("Ошибка", response != null ? response : "Не удалось обновить финансовую информацию");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректные числовые значения (используйте точку для десятичных чисел, например, 120.00)");
        } catch (Exception e) {
            showAlert("Ошибка соединения", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    // Удаление финансовой информации
    private void deleteFinancialInfo() {
        if (currentFinancialInfoId == -1) {
            showAlert("Ошибка", "Нет финансовой информации для удаления");
            return;
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("DELETE_FINANCIAL_INFO " + currentFinancialInfoId);
            String response = in.readLine();
            if (response != null && response.startsWith("OK")) {
                showAlert("Успех", "Финансовая информация удалена");
                Stage stage = (Stage) deleteButton.getScene().getWindow();
                stage.close();
                new InterfaceMenu.AdminMain().start(new Stage());
            } else {
                showAlert("Ошибка", response != null ? response : "Не удалось удалить финансовую информацию");
            }
        } catch (Exception e) {
            showAlert("Ошибка соединения", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    // Очистка формы
    private void clearForm() {
        creditBalanceField.clear();
        overdueAmountField.clear();
        openCreditsField.clear();
        creditInquiriesField.clear();
        creditLimitField.clear();
        creditTypesField.clear();
        firstCreditDateField.clear();
    }

    // Отображение уведомлений
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