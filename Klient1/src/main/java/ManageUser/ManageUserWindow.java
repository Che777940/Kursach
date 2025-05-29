package ManageUser;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ManageUserWindow extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate CURRENT_DATE = LocalDate.of(2025, 4, 21);
    private static final int MINIMUM_AGE = 22;
    private static final Pattern BELARUS_PHONE_PATTERN = Pattern.compile("^\\+375(29|33|44|25|17)\\d{7}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@(mail\\.ru|gmail\\.com)$");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Управление пользователями");

        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        primaryStage.setMaximized(true);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Label titleLabel = new Label("===== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        Button viewUsersButton = createStyledButton("Просмотреть список пользователей", e -> {
            primaryStage.close();
            viewUsers();
        });
        Button addUsersButton = createStyledButton("Добавить пользователя", e -> {
            primaryStage.close();
            addUsers(new Stage());
        });
        Button editUsersButton = createStyledButton("Отредактировать информацию о пользователе", e -> {
            primaryStage.close();
            editUsers();
        });
        Button removeUsersButton = createStyledButton("Удаление пользователя", e -> {
            primaryStage.close();
            removeUsers();
        });
        Button returnButton = createStyledButton("Назад", e -> {
            primaryStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

        layout.getChildren().addAll(titleLabel, viewUsersButton, addUsersButton, editUsersButton,
                removeUsersButton, returnButton);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a3c6d; -fx-background: #1a3c6d;");

        Scene scene = new Scene(scrollPane);
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

    private void editUsers() {
        Stage editStage = new Stage();
        editStage.setTitle("Поиск пользователя для редактирования");

        try {
            editStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/bank_icon.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        editStage.setMaximized(true);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Label titleLabel = new Label("Редактирование пользователя");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff;"
        );

        Label instructionLabel = new Label("Найдите пользователя для редактирования:");
        instructionLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );

        TextField searchField = new TextField();
        searchField.setPromptText("Введите ID или часть имени");
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
                        "-fx-max-width: 300px;"
        );

        TableView<User> searchResultsTable = new TableView<>();
        searchResultsTable.setPrefHeight(400);
        searchResultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        searchResultsTable.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px;"
        );

        TableColumn<User, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        idColumn.setPrefWidth(100);
        idColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<User, String> nameColumn = new TableColumn<>("ФИО");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().FullNameProperty());
        nameColumn.setPrefWidth(300);
        nameColumn.setStyle("-fx-alignment: CENTER;");

        searchResultsTable.getColumns().addAll(idColumn, nameColumn);

        ObservableList<User> allUsers = FXCollections.observableArrayList();
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_ALL_USERS");
            String response = in.readLine();

            if (response != null && response.startsWith("OK|")) {
                String[] usersData = response.substring(3).split("\\|");
                for (String userData : usersData) {
                    String[] parts = userData.split(":");
                    if (parts.length >= 2) {
                        allUsers.add(new User(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось загрузить список пользователей: " + e.getMessage());
        }

        FilteredList<User> filteredUsers = new FilteredList<>(allUsers, p -> true);
        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(searchResultsTable.comparatorProperty());
        searchResultsTable.setItems(sortedUsers);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return false;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return user.getID().toLowerCase().contains(lowerCaseFilter) ||
                        user.getFullName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        Button searchButton = createStyledButton("Найти", e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                filteredUsers.setPredicate(user ->
                        user.getID().toLowerCase().contains(searchText.toLowerCase()) ||
                                user.getFullName().toLowerCase().contains(searchText.toLowerCase())
                );
            }
        });

        searchResultsTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    User selectedUser = row.getItem();
                    editStage.close();
                    loadUserForEdit(selectedUser.getID());
                }
            });
            return row;
        });

        Button cancelButton = createStyledButton("Отмена", e -> {
            editStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                titleLabel,
                instructionLabel,
                searchBox,
                searchResultsTable,
                cancelButton
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a3c6d; -fx-background: #1a3c6d;");

        Scene scene = new Scene(scrollPane);
        editStage.setScene(scene);
        editStage.show();
    }

    private void loadUserForEdit(String userId) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_USER_DETAILS " + userId);
            String response = in.readLine();

            if (response != null && response.startsWith("OK|")) {
                showEditForm(response.substring(3).split("\\|"));
            } else {
                showAlert("Ошибка", response != null ? response : "Не удалось получить данные пользователя");
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка соединения: " + e.getMessage());
        }
    }

    private void showEditForm(String[] userData) {
        Stage editFormStage = new Stage();
        editFormStage.setTitle("Редактирование данных пользователя");

        try {
            editFormStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/bank_icon.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        editFormStage.setMaximized(true);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30));
        grid.setHgap(15);
        grid.setVgap(15);

        TextField fullNameField = new TextField(userData[1]);
        DatePicker birthDatePicker = new DatePicker(LocalDate.parse(userData[2]));
        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("М", "Ж");
        genderComboBox.setValue(userData[3]);
        TextField phoneField = new TextField(userData[4]);
        TextField emailField = new TextField(userData[5]);
        TextField addressField = new TextField(userData[6]);
        TextField incomeField = new TextField(userData[7]);
        TextField employmentField = new TextField(userData[8]);
        TextField workplaceField = new TextField(userData[9]);
        TextArea assetsArea = new TextArea(userData[10]);
        assetsArea.setPrefHeight(100);
        TextField passportNumberField = new TextField(userData[11]);
        DatePicker passportIssueDatePicker = new DatePicker(userData[12].isEmpty() ? null : LocalDate.parse(userData[12]));
        DatePicker passportExpiryDatePicker = new DatePicker(userData[13].isEmpty() ? null : LocalDate.parse(userData[13]));
        TextField passportIssuingCountryField = new TextField(userData[14]);
        Button saveButton = createStyledButton("Сохранить", null);
        Button backButton = createStyledButton("Назад", e -> {
            editFormStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

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

        fullNameField.setStyle(fieldStyle);
        birthDatePicker.setStyle(fieldStyle);
        genderComboBox.setStyle(fieldStyle);
        phoneField.setStyle(fieldStyle);
        emailField.setStyle(fieldStyle);
        addressField.setStyle(fieldStyle);
        incomeField.setStyle(fieldStyle);
        employmentField.setStyle(fieldStyle);
        workplaceField.setStyle(fieldStyle);
        assetsArea.setStyle(fieldStyle);
        passportNumberField.setStyle(fieldStyle);
        passportIssueDatePicker.setStyle(fieldStyle);
        passportExpiryDatePicker.setStyle(fieldStyle);
        passportIssuingCountryField.setStyle(fieldStyle);

        int row = 0;
        addEditableRow(grid, "ФИО:", fullNameField, row++);
        addEditableRow(grid, "Дата рождения:", birthDatePicker, row++);
        addEditableRow(grid, "Пол:", genderComboBox, row++);
        addEditableRow(grid, "Телефон:", phoneField, row++);
        addEditableRow(grid, "Email:", emailField, row++);
        addEditableRow(grid, "Адрес:", addressField, row++);
        addEditableRow(grid, "Доход:", incomeField, row++);
        addEditableRow(grid, "Статус занятости:", employmentField, row++);
        addEditableRow(grid, "Место работы:", workplaceField, row++);
        addEditableRow(grid, "Активы:", assetsArea, row++);
        addEditableRow(grid, "Номер паспорта:", passportNumberField, row++);
        addEditableRow(grid, "Дата выдачи паспорта:", passportIssueDatePicker, row++);
        addEditableRow(grid, "Дата окончания паспорта:", passportExpiryDatePicker, row++);
        addEditableRow(grid, "Страна выдачи паспорта:", passportIssuingCountryField, row++);
        grid.add(saveButton, 1, row++);
        grid.add(backButton, 1, row);

        saveButton.setOnAction(e -> {
            if (fullNameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() || addressField.getText().trim().isEmpty() ||
                    incomeField.getText().trim().isEmpty() || employmentField.getText().trim().isEmpty() ||
                    genderComboBox.getValue() == null || birthDatePicker.getValue() == null) {
                showAlert("Ошибка", "Заполните все обязательные поля");
                return;
            }

            LocalDate birthDate = birthDatePicker.getValue();
            int age = Period.between(birthDate, CURRENT_DATE).getYears();
            if (age < MINIMUM_AGE) {
                showAlert("Ошибка", "Пользователю должно быть не менее 22 лет");
                return;
            }

            String phone = phoneField.getText().trim();
            if (!BELARUS_PHONE_PATTERN.matcher(phone).matches()) {
                showAlert("Ошибка", "Телефон должен быть в формате +375 (29, 33, 44, 25, 17) и 7 цифр, например: +375291234567");
                return;
            }

            String email = emailField.getText().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                showAlert("Ошибка", "Email должен заканчиваться на @mail.ru или @gmail.com");
                return;
            }

            String incomeText = incomeField.getText().trim().replace(',', '.');
            try {
                new BigDecimal(incomeText);
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Некорректный формат дохода. Используйте числа (например: 25000 или 35000.50)");
                return;
            }

            try {
                List<String> params = new ArrayList<>();
                params.add("UPDATE_USER");
                params.add(userData[0]);
                params.add(quoteIfNeeded(fullNameField.getText().trim()));
                params.add(birthDatePicker.getValue().format(DATE_FORMATTER));
                params.add(quoteIfNeeded(genderComboBox.getValue()));
                params.add(quoteIfNeeded(phoneField.getText().trim()));
                params.add(quoteIfNeeded(emailField.getText().trim()));
                params.add(quoteIfNeeded(addressField.getText().trim()));
                params.add(incomeText);
                params.add(quoteIfNeeded(employmentField.getText().trim()));
                params.add(workplaceField.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(workplaceField.getText().trim()));
                params.add(assetsArea.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(assetsArea.getText().trim()));
                params.add(passportNumberField.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(passportNumberField.getText().trim()));
                params.add(passportIssueDatePicker.getValue() == null ? "NULL" : passportIssueDatePicker.getValue().format(DATE_FORMATTER));
                params.add(passportExpiryDatePicker.getValue() == null ? "NULL" : passportExpiryDatePicker.getValue().format(DATE_FORMATTER));
                params.add(passportIssuingCountryField.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(passportIssuingCountryField.getText().trim()));

                String command = String.join(" ", params);

                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    out.println(command);
                    String response = in.readLine();

                    if (response != null && response.startsWith("OK:")) {
                        showAlert("Успех", "Данные пользователя успешно обновлены");
                        editFormStage.close();
                        new InterfaceMenu.AdminMain().start(new Stage());
                    } else {
                        showAlert("Ошибка", response != null ? response : "Не удалось обновить данные");
                    }
                }
            } catch (Exception ex) {
                showAlert("Ошибка", "Ошибка при обновлении: " + ex.getMessage());
            }
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a3c6d; -fx-background: #1a3c6d;");

        Scene scene = new Scene(scrollPane);
        editFormStage.setScene(scene);
        editFormStage.show();
    }

    private void addEditableRow(GridPane grid, String label, Control field, int row) {
        Label lbl = new Label(label);
        lbl.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }

    private void removeUsers() {
        Stage removeStage = new Stage();
        removeStage.setTitle("Удаление пользователя");

        try {
            removeStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/bank_icon.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        removeStage.setMaximized(true);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Label titleLabel = new Label("Удаление пользователя");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff;"
        );

        Label instructionLabel = new Label("Найдите пользователя для удаления:");
        instructionLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );

        TextField searchField = new TextField();
        searchField.setPromptText("Введите ID или часть имени");
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
                        "-fx-max-width: 300px;"
        );

        TableView<User> searchResultsTable = new TableView<>();
        searchResultsTable.setPrefHeight(400);
        searchResultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        searchResultsTable.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px;"
        );

        TableColumn<User, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        idColumn.setPrefWidth(100);
        idColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<User, String> nameColumn = new TableColumn<>("ФИО");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().FullNameProperty());
        nameColumn.setPrefWidth(300);
        nameColumn.setStyle("-fx-alignment: CENTER;");

        searchResultsTable.getColumns().addAll(idColumn, nameColumn);

        ObservableList<User> allUsers = FXCollections.observableArrayList();
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_ALL_USERS");
            String response = in.readLine();

            if (response != null && response.startsWith("OK|")) {
                String[] usersData = response.substring(3).split("\\|");
                for (String userData : usersData) {
                    String[] parts = userData.split(":");
                    if (parts.length >= 2) {
                        allUsers.add(new User(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось загрузить список пользователей: " + e.getMessage());
        }

        FilteredList<User> filteredUsers = new FilteredList<>(allUsers, p -> true);
        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(searchResultsTable.comparatorProperty());
        searchResultsTable.setItems(sortedUsers);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return false;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return user.getID().toLowerCase().contains(lowerCaseFilter) ||
                        user.getFullName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        Button searchButton = createStyledButton("Найти", e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                filteredUsers.setPredicate(user ->
                        user.getID().toLowerCase().contains(searchText.toLowerCase()) ||
                                user.getFullName().toLowerCase().contains(searchText.toLowerCase())
                );
            }
        });

        Button deleteButton = createStyledButton("Удалить выбранного", null);
        deleteButton.setDisable(true);

        searchResultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteButton.setDisable(newSelection == null);
        });

        deleteButton.setOnAction(e -> {
            User selectedUser = searchResultsTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Подтверждение удаления");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("Вы уверены, что хотите удалить пользователя:\n" +
                        selectedUser.getFullName() + " (ID: " + selectedUser.getID() + ")?");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                        out.println("DELETE_USER " + selectedUser.getID());
                        String response = in.readLine();

                        if (response != null && response.startsWith("OK:")) {
                            showAlert("Успех", "Пользователь успешно удален");
                            allUsers.remove(selectedUser);
                            searchField.clear();
                            removeStage.close();
                            new InterfaceMenu.AdminMain().start(new Stage());
                        } else {
                            showAlert("Ошибка", response != null ? response : "Не удалось удалить пользователя");
                        }
                    } catch (IOException ex) {
                        showAlert("Ошибка", "Ошибка соединения: " + ex.getMessage());
                    }
                }
            }
        });

        Button cancelButton = createStyledButton("Отмена", e -> {
            removeStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(10, deleteButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                titleLabel,
                instructionLabel,
                searchBox,
                searchResultsTable,
                buttonBox
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a3c6d; -fx-background: #1a3c6d;");

        Scene scene = new Scene(scrollPane);
        removeStage.setScene(scene);
        removeStage.show();
    }

    private void addUsers(Stage addUserStage) {
        addUserStage.setTitle("Добавление пользователя");

        try {
            addUserStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/bank_icon.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        addUserStage.setMaximized(true);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Label titleLabel = new Label("Добавление нового клиента:");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff;"
        );

        TextField fullNameField = createTextField("Полное имя*");
        DatePicker birthDatePicker = createDatePicker();
        ComboBox<String> genderComboBox = createComboBox(new String[]{"М", "Ж"}, "Пол*");
        TextField phoneField = createTextField("Телефон* (например: +375291234567)");
        TextField emailField = createTextField("Email* (только @mail.ru или @gmail.com)");
        TextField addressField = createTextField("Адрес*");
        TextField incomeField = createTextField("Ежемесячный доход*");
        TextField employmentField = createTextField("Статус занятости*");
        TextField workplaceField = createTextField("Место работы");
        Label assetsLabel = new Label("Активы:");
        assetsLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        TextArea assetsArea = new TextArea();
        assetsArea.setPromptText("Активы (через запятую)");
        assetsArea.setPrefHeight(100);
        assetsArea.setStyle(
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
        TextField passportNumberField = createTextField("Номер паспорта");
        DatePicker passportIssueDatePicker = createDatePicker("Дата выдачи паспорта");
        DatePicker passportExpiryDatePicker = createDatePicker("Дата окончания паспорта");
        TextField passportIssuingCountryField = createTextField("Страна выдачи паспорта");

        Button submitButton = createStyledButton("Добавить клиента", e -> handleUserAddition(
                fullNameField, birthDatePicker, genderComboBox, phoneField,
                emailField, addressField, incomeField,
                employmentField, workplaceField, assetsArea,
                passportNumberField, passportIssueDatePicker, passportExpiryDatePicker,
                passportIssuingCountryField, addUserStage));

        Button backButton = createStyledButton("Назад", e -> {
            addUserStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

        layout.getChildren().addAll(
                titleLabel,
                fullNameField, birthDatePicker, genderComboBox,
                phoneField, emailField, addressField, incomeField,
                employmentField, workplaceField, assetsLabel, assetsArea,
                passportNumberField, passportIssueDatePicker, passportExpiryDatePicker,
                passportIssuingCountryField, submitButton, backButton);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a3c6d; -fx-background: #1a3c6d;");

        Scene scene = new Scene(scrollPane);
        addUserStage.setScene(scene);
        addUserStage.show();
    }

    private void handleUserAddition(TextField fullName, DatePicker birthDate, ComboBox<String> gender,
                                    TextField phone, TextField email, TextField address,
                                    TextField income, TextField employment,
                                    TextField workplace, TextArea assets,
                                    TextField passportNumber, DatePicker passportIssueDate,
                                    DatePicker passportExpiryDate, TextField passportIssuingCountry,
                                    Stage stage) {
        if (fullName.getText().trim().isEmpty() ||
                birthDate.getValue() == null ||
                gender.getValue() == null ||
                phone.getText().trim().isEmpty() ||
                email.getText().trim().isEmpty() ||
                address.getText().trim().isEmpty() ||
                income.getText().trim().isEmpty() ||
                employment.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Пожалуйста, заполните все обязательные поля (помечены *)");
            return;
        }

        LocalDate birthDateValue = birthDate.getValue();
        int age = Period.between(birthDateValue, CURRENT_DATE).getYears();
        if (age < MINIMUM_AGE) {
            showAlert("Ошибка", "Пользователю должно быть не менее 22 лет");
            return;
        }

        String phoneText = phone.getText().trim();
        if (!BELARUS_PHONE_PATTERN.matcher(phoneText).matches()) {
            showAlert("Ошибка", "Телефон должен быть в формате +375 (29, 33, 44, 25, 17) и 7 цифр, например: +375291234567");
            return;
        }

        String emailText = email.getText().trim();
        if (!EMAIL_PATTERN.matcher(emailText).matches()) {
            showAlert("Ошибка", "Email должен заканчиваться на @mail.ru или @gmail.com");
            return;
        }

        String incomeText = income.getText().trim().replace(',', '.');
        try {
            new BigDecimal(incomeText);
        } catch (NumberFormatException ex) {
            showAlert("Ошибка", "Некорректный формат дохода. Используйте числа (например: 25000 или 35000.50)");
            return;
        }

        try {
            List<String> params = new ArrayList<>();
            params.add("ADD_CLIENT");
            params.add(quoteIfNeeded(fullName.getText().trim()));
            params.add(birthDate.getValue().format(DATE_FORMATTER));
            params.add(gender.getValue());
            params.add(quoteIfNeeded(phone.getText().trim()));
            params.add(quoteIfNeeded(email.getText().trim()));
            params.add(quoteIfNeeded(address.getText().trim()));
            params.add(incomeText);
            params.add(quoteIfNeeded(employment.getText().trim()));
            params.add(workplace.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(workplace.getText().trim()));
            params.add(assets.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(assets.getText().trim()));
            params.add(passportNumber.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(passportNumber.getText().trim()));
            params.add(passportIssueDate.getValue() == null ? "NULL" : passportIssueDate.getValue().format(DATE_FORMATTER));
            params.add(passportExpiryDate.getValue() == null ? "NULL" : passportExpiryDate.getValue().format(DATE_FORMATTER));
            params.add(passportIssuingCountry.getText().trim().isEmpty() ? "NULL" : quoteIfNeeded(passportIssuingCountry.getText().trim()));

            String command = String.join(" ", params);
            System.out.println("Отправляемая команда: " + command);

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(command);
                String response = in.readLine();

                if (response != null && response.startsWith("OK")) {
                    showAlert("Успех", "Клиент успешно добавлен!");
                    stage.close();
                    new InterfaceMenu.AdminMain().start(new Stage());
                } else {
                    showAlert("Ошибка", response != null ? response : "Неизвестная ошибка сервера");
                }
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    private String quoteIfNeeded(String value) {
        if (value == null || value.equals("NULL")) {
            return "NULL";
        }
        if (value.contains(" ") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\\\"") + "\"";
        }
        return value;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle(
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
        return textField;
    }

    private DatePicker createDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Выберите дату");
        datePicker.setStyle(
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
        return datePicker;
    }

    private DatePicker createDatePicker(String promptText) {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText(promptText);
        datePicker.setStyle(
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
        return datePicker;
    }

    private ComboBox<String> createComboBox(String[] items, String promptText) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.setPromptText(promptText);
        comboBox.setStyle(
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
        return comboBox;
    }

    private void viewUsers() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_ALL_USERS");
            String response = in.readLine();

            if (response != null && response.startsWith("OK|")) {
                showUsersTable(response.substring(3).split("\\|"));
            } else {
                showAlert("Ошибка", response != null ? response : "Не удалось получить список пользователей");
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка подключения: " + e.getMessage());
        }
    }

    public static class User {
        private final SimpleStringProperty ID;
        private final SimpleStringProperty FullName;

        public User(String ID, String FullName) {
            this.ID = new SimpleStringProperty(ID);
            this.FullName = new SimpleStringProperty(FullName);
        }

        public String getID() { return ID.get(); }
        public String getFullName() { return FullName.get(); }

        public SimpleStringProperty IDProperty() { return ID; }
        public SimpleStringProperty FullNameProperty() { return FullName; }
    }

    private void showUsersTable(String[] usersData) {
        Stage tableStage = new Stage();
        tableStage.setTitle("Список пользователей");

        try {
            tableStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/bank_icon.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        tableStage.setMaximized(true);

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(30));

        HBox searchPanel = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Введите ID или часть имени");
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
                        "-fx-max-width: 300px;"
        );

        Button searchButton = createStyledButton("Поиск", null);
        Button resetButton = createStyledButton("Сброс", null);
        Button backButton = createStyledButton("Назад", e -> {
            tableStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });

        searchPanel.getChildren().addAll(searchField, searchButton, resetButton);
        searchPanel.setAlignment(Pos.CENTER);

        TableView<User> tableView = new TableView<>();
        tableView.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #1a3c6d; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 10px;"
        );

        TableColumn<User, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        idColumn.setPrefWidth(100);
        idColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<User, String> nameColumn = new TableColumn<>("ФИО");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().FullNameProperty());
        nameColumn.setPrefWidth(300);
        nameColumn.setStyle("-fx-alignment: CENTER;");

        tableView.getColumns().addAll(idColumn, nameColumn);

        ObservableList<User> users = FXCollections.observableArrayList();
        for (String userData : usersData) {
            String[] parts = userData.split(":");
            if (parts.length >= 2) {
                users.add(new User(parts[0].trim(), parts[1].trim()));
            }
        }

        FilteredList<User> filteredUsers = new FilteredList<>(users, p -> true);
        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedUsers);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getID().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getFullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        searchButton.setOnAction(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                filteredUsers.setPredicate(user ->
                        user.getID().toLowerCase().contains(searchText.toLowerCase()) ||
                                user.getFullName().toLowerCase().contains(searchText.toLowerCase())
                );
            }
        });

        resetButton.setOnAction(e -> {
            searchField.clear();
            filteredUsers.setPredicate(null);
        });

        tableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    tableStage.close();
                    loadAndShowUserDetails(row.getItem().getID());
                }
            });
            return row;
        });

        vbox.getChildren().addAll(searchPanel, tableView, backButton);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color-fx-background-color: #1a3c6d; -fx-background: #1a3c6d;");

        Scene scene = new Scene(scrollPane);
        tableStage.setScene(scene);
        tableStage.show();
    }

    private void loadAndShowUserDetails(String userId) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_USER_DETAILS " + userId);

            String response = in.readLine();
            if (response != null && response.startsWith("OK|")) {
                showUserDetails(response.substring(3).split("\\|"));
            } else {
                showAlert("Ошибка", response != null ? response : "Не удалось получить данные");
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка соединения: " + e.getMessage());
        }
    }

    private void showUserDetails(String[] userData) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Детали пользователя");

        try {
            detailsStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/bank_icon.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        detailsStage.setMaximized(true);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30));
        grid.setHgap(15);
        grid.setVgap(15);

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

        int row = 0;
        addDetailRow(grid, "ID:", userData[0], row++, fieldStyle);
        addDetailRow(grid, "ФИО:", userData[1], row++, fieldStyle);
        addDetailRow(grid, "Дата рождения:", userData[2], row++, fieldStyle);
        addDetailRow(grid, "Пол:", userData[3], row++, fieldStyle);
        addDetailRow(grid, "Телефон:", userData[4], row++, fieldStyle);
        addDetailRow(grid, "Email:", userData[5], row++, fieldStyle);
        addDetailRow(grid, "Адрес:", userData[6], row++, fieldStyle);
        addDetailRow(grid, "Доход:", userData[7], row++, fieldStyle);
        addDetailRow(grid, "Статус занятости:", userData[8], row++, fieldStyle);
        addDetailRow(grid, "Место работы:", userData[9], row++, fieldStyle);
        addDetailRow(grid, "Активы:", userData[10], row++, fieldStyle);
        addDetailRow(grid, "Номер паспорта:", userData[11], row++, fieldStyle);
        addDetailRow(grid, "Дата выдачи паспорта:", userData[12], row++, fieldStyle);
        addDetailRow(grid, "Дата окончания паспорта:", userData[13], row++, fieldStyle);
        addDetailRow(grid, "Страна выдачи паспорта:", userData[14], row++, fieldStyle);

        Button backButton = createStyledButton("Назад", e -> {
            detailsStage.close();
            new InterfaceMenu.AdminMain().start(new Stage());
        });
        grid.add(backButton, 1, row);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a3c6d; -fx-background: #1a3c6d;");

        Scene scene = new Scene(scrollPane);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    private void addDetailRow(GridPane grid, String label, String value, int row, String fieldStyle) {
        Label lbl = new Label(label);
        lbl.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #ffffff;"
        );
        grid.add(lbl, 0, row);

        TextField tf = new TextField(value);
        tf.setEditable(false);
        tf.setStyle(fieldStyle);
        grid.add(tf, 1, row);
    }

    public static void main(String[] args) {
        launch(args);
    }
}