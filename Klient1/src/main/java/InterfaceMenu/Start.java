package InterfaceMenu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.geometry.Insets;

public class Start extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Личный кабинет");

        // Установка иконки окна
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        primaryStage.setMaximized(true);

        // Заголовок
        Label titleLabel = new Label("===== ЛИЧНЫЙ КАБИНЕТ =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        // Кнопки
        Button registerButton = new Button("Регистрация");
        Button loginButton = new Button("Авторизация");
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
        loginButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        // Эффекты при наведении
        registerButton.setOnMouseEntered(e -> registerButton.setStyle(buttonStyle + buttonHoverStyle));
        registerButton.setOnMouseExited(e -> registerButton.setStyle(buttonStyle));
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(buttonStyle + buttonHoverStyle));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(buttonStyle));
        exitButton.setOnMouseEntered(e -> exitButton.setStyle(buttonStyle + buttonHoverStyle));
        exitButton.setOnMouseExited(e -> exitButton.setStyle(buttonStyle));

        // Обработчики кнопок с закрытием текущего окна
        registerButton.setOnAction(e -> {
            // Закрываем текущее окно перед открытием нового
            primaryStage.close();
            // Предполагаем, что Registration принимает Stage для закрытия
            new Registration().start(new Stage());
        });

        loginButton.setOnAction(e -> {
            // Закрываем текущее окно перед открытием нового
            primaryStage.close();
            // Предполагаем, что Authorisation принимает Stage для закрытия
            new Authorisation().start(new Stage());
        });

        exitButton.setOnAction(e -> primaryStage.close());

        // Компоновка
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(titleLabel, registerButton, loginButton, exitButton);
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Фон окна
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(layout);

        // Сцена
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}