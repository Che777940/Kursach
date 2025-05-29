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

public class Authorisation extends Application {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    private TextField usernameField;
    private PasswordField passwordField;
    private Label responseLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Авторизация");

        // Установка иконки окна
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        primaryStage.setMaximized(true);

        // Заголовок
        Label titleLabel = new Label("===== МЕНЮ АВТОРИЗАЦИИ =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        // Поля ввода
        usernameField = new TextField();
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

        passwordField = new PasswordField();
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

        // Кнопки
        Button loginButton = new Button("Войти");
        Button adminLoginButton = new Button("Войти как администратор");
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

        loginButton.setStyle(buttonStyle);
        adminLoginButton.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);

        // Эффекты при наведении
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(buttonStyle + buttonHoverStyle));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(buttonStyle));
        adminLoginButton.setOnMouseEntered(e -> adminLoginButton.setStyle(buttonStyle + buttonHoverStyle));
        adminLoginButton.setOnMouseExited(e -> adminLoginButton.setStyle(buttonStyle));
        backButton.setOnMouseEntered(e -> backButton.setStyle(buttonStyle + buttonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(buttonStyle));

        // Метка для ответа
        responseLabel = new Label();
        responseLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 10px;"
        );

        // Обработчики кнопок с закрытием текущего окна
        loginButton.setOnAction(e -> {
            login(1);
            if (responseLabel.getText().startsWith("Успешный вход")) {
                primaryStage.close();
            }
        });

        adminLoginButton.setOnAction(e -> {
            login(2);
            if (responseLabel.getText().startsWith("Успешный вход")) {
                primaryStage.close();
            }
        });

        backButton.setOnAction(e -> {
            primaryStage.close();
            new Start().start(new Stage());
        });

        // Компоновка
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, adminLoginButton, backButton, responseLabel);

        // Фон окна
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(layout);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void login(int userType) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            responseLabel.setText("Ошибка: Логин и пароль не могут быть пустыми.");
            responseLabel.setStyle(
                    "-fx-font-family: 'Arial'; " +
                            "-fx-font-size: 14px; " +
                            "-fx-text-fill: #ff4d4d; " +
                            "-fx-padding: 10px;"
            );
            return;
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("LOGIN " + username + " " + password);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String role = response.split(" ")[1];
                if (role.equals("user") && userType == 1) {
                    responseLabel.setText("Успешный вход! Добро пожаловать, пользователь.");
                    responseLabel.setStyle(
                            "-fx-font-family: 'Arial'; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-text-fill: #4dff4d; " +
                                    "-fx-padding: 10px;"
                    );
                    new UserMain().start(new Stage());
                } else if (role.equals("admin") && userType == 2) {
                    responseLabel.setText("Успешный вход! Добро пожаловать, администратор.");
                    responseLabel.setStyle(
                            "-fx-font-family: 'Arial'; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-text-fill: #4dff4d; " +
                                    "-fx-padding: 10px;"
                    );
                    new AdminMain().start(new Stage());
                } else {
                    responseLabel.setText("Ошибка: У вас нет прав для этого входа.");
                    responseLabel.setStyle(
                            "-fx-font-family: 'Arial'; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-text-fill: #ff4d4d; " +
                                    "-fx-padding: 10px;"
                    );
                }
            } else {
                responseLabel.setText(response);
                responseLabel.setStyle(
                        "-fx-font-family: 'Arial'; " +
                                "-fx-font-size: 14px; " +
                                "-fx-text-fill: #ff4d4d; " +
                                "-fx-padding: 10px;"
                );
            }
        } catch (IOException e) {
            responseLabel.setText("Ошибка подключения к серверу: " + e.getMessage());
            responseLabel.setStyle(
                    "-fx-font-family: 'Arial'; " +
                            "-fx-font-size: 14px; " +
                            "-fx-text-fill: #ff4d4d; " +
                            "-fx-padding: 10px;"
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}