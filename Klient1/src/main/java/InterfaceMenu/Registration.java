package InterfaceMenu;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.*;
import java.net.Socket;

public class Registration extends Application {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Регистрация");

        // Установка иконки окна
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        primaryStage.setMaximized(true);

        // Создание элементов интерфейса
        Label titleLabel = new Label("===== МЕНЮ РЕГИСТРАЦИИ =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        TextField usernameField = new TextField();
        usernameField.setPromptText("Введите логин");
        usernameField.setStyle(
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

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setStyle(
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

        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton userRadio = new RadioButton("Пользователь");
        userRadio.setToggleGroup(roleGroup);
        userRadio.setSelected(true);
        userRadio.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 5px;"
        );

        RadioButton adminRadio = new RadioButton("Администратор");
        adminRadio.setToggleGroup(roleGroup);
        adminRadio.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 5px;"
        );

        // Кнопки
        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");
        Button exitButton = new Button("Выход");

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

        registerButton.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        // Эффекты при наведении
        registerButton.setOnMouseEntered(e -> registerButton.setStyle(buttonStyle + buttonHoverStyle));
        registerButton.setOnMouseExited(e -> registerButton.setStyle(buttonStyle));
        backButton.setOnMouseEntered(e -> backButton.setStyle(buttonStyle + buttonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(buttonStyle));
        exitButton.setOnMouseEntered(e -> exitButton.setStyle(buttonStyle + buttonHoverStyle));
        exitButton.setOnMouseExited(e -> exitButton.setStyle(buttonStyle));

        // Обработчики кнопок
        registerButton.setOnAction(e -> handleRegistration(usernameField, passwordField, roleGroup, primaryStage));

        backButton.setOnAction(e -> {
            primaryStage.close();
            new Start().start(new Stage());
        });

        exitButton.setOnAction(e -> primaryStage.close());

        // Компоновка
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(titleLabel, usernameField, passwordField, userRadio, adminRadio, registerButton, backButton, exitButton);
        layout.setAlignment(Pos.CENTER);

        // Фон окна
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(layout);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleRegistration(TextField usernameField, PasswordField passwordField, ToggleGroup roleGroup, Stage currentStage) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        RadioButton selectedRole = (RadioButton) roleGroup.getSelectedToggle();
        String role = selectedRole.getText().equals("Пользователь") ? "user" : "admin";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Пожалуйста, заполните все поля.");
            return;
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("REGISTER " + username + " " + password + " " + role);
            String response = in.readLine();

            showAlert("Ответ сервера", response);
            if (response.startsWith("OK")) {
                currentStage.close();
            }

        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка подключения к серверу: " + e.getMessage());
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