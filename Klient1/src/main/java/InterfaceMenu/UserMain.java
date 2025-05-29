package InterfaceMenu;

import InterfaceMenu.UserActions.ApplyLoanWindow;
import InterfaceMenu.UserActions.CheckStatusWindow;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class UserMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Меню Пользователя");

        // Установка иконки окна
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        primaryStage.setMaximized(true);

        Label titleLabel = new Label("===== МЕНЮ ПОЛЬЗОВАТЕЛЯ (ЗАЕМЩИКА) =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        Button applyLoanButton = new Button("Подать заявку на кредит");
        Button checkStatusButton = new Button("Просмотреть статус заявки");
        Button contactSupportButton = new Button("Связаться с поддержкой");
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

        applyLoanButton.setStyle(buttonStyle);
        checkStatusButton.setStyle(buttonStyle);
        contactSupportButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        applyLoanButton.setOnMouseEntered(e -> applyLoanButton.setStyle(buttonStyle + buttonHoverStyle));
        applyLoanButton.setOnMouseExited(e -> applyLoanButton.setStyle(buttonStyle));
        checkStatusButton.setOnMouseEntered(e -> checkStatusButton.setStyle(buttonStyle + buttonHoverStyle));
        checkStatusButton.setOnMouseExited(e -> checkStatusButton.setStyle(buttonStyle));
        contactSupportButton.setOnMouseEntered(e -> contactSupportButton.setStyle(buttonStyle + buttonHoverStyle));
        contactSupportButton.setOnMouseExited(e -> contactSupportButton.setStyle(buttonStyle));
        exitButton.setOnMouseEntered(e -> exitButton.setStyle(buttonStyle + buttonHoverStyle));
        exitButton.setOnMouseExited(e -> exitButton.setStyle(buttonStyle));

        applyLoanButton.setOnAction(e -> {
            primaryStage.close();
            new ApplyLoanWindow();
        });
        checkStatusButton.setOnAction(e -> {
            primaryStage.close();
            new CheckStatusWindow();
        });
        contactSupportButton.setOnAction(e -> contactSupport());
        exitButton.setOnAction(e -> {
            primaryStage.close();
            new Authorisation().start(new Stage());
        });

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(
                titleLabel,
                applyLoanButton,
                checkStatusButton,
                contactSupportButton,
                exitButton
        );
        layout.setAlignment(Pos.CENTER);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(layout);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void viewCreditHistory() {
        showAlert("Просмотр кредитной истории", "Здесь будет отображена ваша кредитная история.");
    }

    private void calculateCreditScore() {
        showAlert("Расчет кредитного рейтинга", "Здесь можно будет рассчитать кредитный рейтинг.");
    }

    private void contactSupport() {
        showAlert("Связь с поддержкой", "Связь с поддержкой.");
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