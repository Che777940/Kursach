package InterfaceMenu;

import FinancialInfoManagement.ClientDTO;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class CalculateFicoScoreWindow extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    private TextField searchField;
    private TableView<ClientDTO> clientTable;
    private Label resultLabel;
    private Button calculateButton;
    private ObservableList<ClientDTO> clients = FXCollections.observableArrayList();
    private ObservableList<ClientDTO> filteredClients = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Расчёт FICO-балла");

        // Установка иконки окна
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        primaryStage.setMaximized(true);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.CENTER);

        // Поле для поиска
        Label searchLabel = new Label("Поиск клиента:");
        searchLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );

        searchField = new TextField();
        searchField.setPromptText("Введите имя клиента");
        searchField.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #1a3c6d; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 10px; " +
                        "-fx-max-width: 400px;"
        );
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterClients(newValue));

        // Таблица клиентов
        clientTable = new TableView<>();
        clientTable.setPlaceholder(new Label("Клиенты не загружены или отсутствуют"));
        clientTable.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px;"
        );
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ClientDTO, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(100);
        idColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<ClientDTO, String> nameColumn = new TableColumn<>("ФИО");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameColumn.setPrefWidth(300);
        nameColumn.setStyle("-fx-alignment: CENTER;");

        clientTable.getColumns().addAll(idColumn, nameColumn);
        clientTable.setItems(filteredClients);

        // Выделение клиента
        clientTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        clientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                calculateButton.setDisable(false);
            } else {
                calculateButton.setDisable(true);
            }
        });

        // Результат расчёта
        resultLabel = new Label("Результат: не рассчитан");
        resultLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 10px;"
        );

        // Кнопки
        calculateButton = new Button("Рассчитать FICO");
        calculateButton.setDisable(true);
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

        calculateButton.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);

        calculateButton.setOnMouseEntered(e -> calculateButton.setStyle(buttonStyle + buttonHoverStyle));
        calculateButton.setOnMouseExited(e -> calculateButton.setStyle(buttonStyle));
        backButton.setOnMouseEntered(e -> backButton.setStyle(buttonStyle + buttonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(buttonStyle));

        calculateButton.setOnAction(e -> calculateFicoScore());

        backButton.setOnAction(e -> {
            primaryStage.close();
            new AdminMain().start(new Stage());
        });

        mainLayout.getChildren().addAll(
                searchLabel,
                searchField,
                clientTable,
                resultLabel,
                calculateButton,
                backButton
        );

        loadClients();

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Загрузка списка клиентов
    private void loadClients() {
        clients.clear();
        filteredClients.clear();
        resultLabel.setText("Результат: не рассчитан");

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            out.println("GET_CLIENTS");
            String response = in.readLine();
            System.out.println("Ответ сервера: " + response); // Отладка

            if (response != null && response.startsWith("OK")) {
                String[] parts = response.split("\\|");
                System.out.println("Количество частей ответа: " + parts.length); // Отладка
                for (int i = 1; i < parts.length; i++) {
                    String[] clientData = parts[i].split(":", 2); // Ограничение на 2 части
                    System.out.println("Часть " + i + ": " + parts[i]); // Отладка
                    if (clientData.length == 2) {
                        try {
                            int id = Integer.parseInt(clientData[0].trim());
                            String fullName = clientData[1].trim();
                            ClientDTO client = new ClientDTO(id, fullName);
                            clients.add(client);
                            System.out.println("Добавлен клиент: ID=" + id + ", ФИО=" + fullName); // Отладка
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка парсинга ID клиента: " + clientData[0]); // Отладка
                        }
                    } else {
                        System.out.println("Неверный формат данных клиента: " + parts[i]); // Отладка
                    }
                }
                filteredClients.setAll(clients);
                clientTable.setItems(filteredClients);
                System.out.println("Всего клиентов загружено: " + clients.size()); // Отладка
                if (clients.isEmpty()) {
                    clientTable.setPlaceholder(new Label("Список клиентов пуст"));
                }
            } else {
                showAlert("Ошибка", response != null ? response : "Нет ответа от сервера");
                clientTable.setPlaceholder(new Label("Ошибка загрузки клиентов"));
            }
        } catch (Exception e) {
            showAlert("Ошибка соединения", "Не удалось подключиться к серверу: " + e.getMessage());
            clientTable.setPlaceholder(new Label("Ошибка соединения с сервером"));
            System.out.println("Исключение при подключении: " + e.getMessage()); // Отладка
        }
    }

    // Фильтрация клиентов по введённому тексту
    private void filterClients(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredClients.setAll(clients);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            List<ClientDTO> filtered = clients.stream()
                    .filter(client -> client.getFullName().toLowerCase().contains(lowerCaseFilter))
                    .collect(Collectors.toList());
            filteredClients.setAll(filtered);
        }
        clientTable.getSelectionModel().clearSelection();
        calculateButton.setDisable(true);
    }

    // Расчёт FICO-балла
    private void calculateFicoScore() {
        ClientDTO selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Ошибка", "Выберите клиента из таблицы");
            return;
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            out.println("CALCULATE_FICO_FOR_CLIENT " + selectedClient.getId());
            String response = in.readLine();
            if (response != null && response.startsWith("OK")) {
                String[] parts = response.split(":");
                if (parts.length == 4) {
                    int ficoScore = Integer.parseInt(parts[2]);
                    String creditApprovalStatus = parts[3];
                    resultLabel.setText("FICO-балл: " + ficoScore + ", Статус: " + creditApprovalStatus);
                    resultLabel.setStyle(
                            "-fx-font-family: 'Arial'; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-text-fill: #4dff4d; " + // Зелёный цвет для успеха
                                    "-fx-padding: 10px;"
                    );
                    showAlert("Успех", "FICO-балл для " + selectedClient.getFullName() + ": " + ficoScore + "\nСтатус: " + creditApprovalStatus);
                } else {
                    showAlert("Ошибка", "Неверный формат ответа от сервера: " + response);
                    resultLabel.setStyle(
                            "-fx-font-family: 'Arial'; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-text-fill: #ff4d4d; " + // Красный цвет для ошибок
                                    "-fx-padding: 10px;"
                    );
                }
            } else {
                resultLabel.setText("Результат: не рассчитан");
                resultLabel.setStyle(
                        "-fx-font-family: 'Arial'; " +
                                "-fx-font-size: 14px; " +
                                "-fx-text-fill: #ff4d4d; " +
                                "-fx-padding: 10px;"
                );
                showAlert("Ошибка", response != null ? response : "Не удалось рассчитать FICO-балл");
            }
        } catch (Exception e) {
            showAlert("Ошибка соединения", "Не удалось подключиться к серверу: " + e.getMessage());
            resultLabel.setStyle(
                    "-fx-font-family: 'Arial'; " +
                            "-fx-font-size: 14px; " +
                            "-fx-text-fill: #ff4d4d; " +
                            "-fx-padding: 10px;"
            );
        }
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