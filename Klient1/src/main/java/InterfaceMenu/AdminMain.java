package InterfaceMenu;

import FinancialInfoManagement.FinancialInfoWindow;
import CreditApplications.CreditApplicationsWindow;
import ScoringWindowParameters.ScoringParameters;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ManageUser.ManageUserWindow;
import javafx.scene.image.Image;

public class AdminMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Меню администратора");

        // Установка иконки окна
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        // Открытие на полный экран
        primaryStage.setMaximized(true);

        Label titleLabel = new Label("===== МЕНЮ АДМИНИСТРАТОРА =====");
        titleLabel.setStyle(
                "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px;"
        );

        Button manageUsersButton = new Button("Управление пользователями");
        Button viewCreditHistoriesButton = new Button("Управление финансовой информацией");
        Button creditApplicationsButton = new Button("Заявки на кредитование");
        Button viewScoringScoreButton = new Button("Расчёт скорингового балла");
        Button configureScoringModelButton = new Button("Настройка параметров скоринговой модели");
        Button manageSecurityLogsButton = new Button("Управление безопасностью и логами");
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
                "-fx-background-color UttingEdge AI: #e6f0ff; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);";

        manageUsersButton.setStyle(buttonStyle);
        viewCreditHistoriesButton.setStyle(buttonStyle);
        creditApplicationsButton.setStyle(buttonStyle);
        viewScoringScoreButton.setStyle(buttonStyle);
        configureScoringModelButton.setStyle(buttonStyle);
        manageSecurityLogsButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        manageUsersButton.setOnMouseEntered(e -> manageUsersButton.setStyle(buttonStyle + buttonHoverStyle));
        manageUsersButton.setOnMouseExited(e -> manageUsersButton.setStyle(buttonStyle));
        viewCreditHistoriesButton.setOnMouseEntered(e -> viewCreditHistoriesButton.setStyle(buttonStyle + buttonHoverStyle));
        viewCreditHistoriesButton.setOnMouseExited(e -> viewCreditHistoriesButton.setStyle(buttonStyle));
        creditApplicationsButton.setOnMouseEntered(e -> creditApplicationsButton.setStyle(buttonStyle + buttonHoverStyle));
        creditApplicationsButton.setOnMouseExited(e -> creditApplicationsButton.setStyle(buttonStyle));
        viewScoringScoreButton.setOnMouseEntered(e -> viewScoringScoreButton.setStyle(buttonStyle + buttonHoverStyle));
        viewScoringScoreButton.setOnMouseExited(e -> viewScoringScoreButton.setStyle(buttonStyle));
        configureScoringModelButton.setOnMouseEntered(e -> configureScoringModelButton.setStyle(buttonStyle + buttonHoverStyle));
        configureScoringModelButton.setOnMouseExited(e -> configureScoringModelButton.setStyle(buttonStyle));
        manageSecurityLogsButton.setOnMouseEntered(e -> manageSecurityLogsButton.setStyle(buttonStyle + buttonHoverStyle));
        manageSecurityLogsButton.setOnMouseExited(e -> manageSecurityLogsButton.setStyle(buttonStyle));
        exitButton.setOnMouseEntered(e -> exitButton.setStyle(buttonStyle + buttonHoverStyle));
        exitButton.setOnMouseExited(e -> exitButton.setStyle(buttonStyle));

        manageUsersButton.setOnAction(e -> {
            primaryStage.close();
            openManageUserWindow();
        });
        viewCreditHistoriesButton.setOnAction(e -> {
            primaryStage.close();
            viewCreditHistories();
        });
        creditApplicationsButton.setOnAction(e -> {
            primaryStage.close();
            manageCreditApplications();
        });
        viewScoringScoreButton.setOnAction(e -> {
            primaryStage.close();
            calculateFicoScore();
        });
        configureScoringModelButton.setOnAction(e -> {
            primaryStage.close();
            configureScoringModel();
        });
        manageSecurityLogsButton.setOnAction(e -> {
            primaryStage.close();
            manageSecurityLogs();
        });
        exitButton.setOnAction(e -> {
            primaryStage.close();
            new Authorisation().start(new Stage());
        });

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(
                titleLabel,
                manageUsersButton,
                viewCreditHistoriesButton,
                creditApplicationsButton,
                viewScoringScoreButton,
                configureScoringModelButton,
                manageSecurityLogsButton,
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

    private void openManageUserWindow() {
        Stage manageUserStage = new Stage();
        new ManageUserWindow().start(manageUserStage);
    }

    private void viewCreditHistories() {
        Stage financialStage = new Stage();
        new FinancialInfoWindow().start(financialStage);
    }

    private void manageCreditApplications() {
        Stage applicationsStage = new Stage();
        new CreditApplicationsWindow().start(applicationsStage);
    }

    private void calculateFicoScore() {
        Stage ficoStage = new Stage();
        new CalculateFicoScoreWindow().start(ficoStage);
    }

    private void configureScoringModel() {
        Stage scoringStage = new Stage();
        new ScoringParameters().start(scoringStage);
    }

    private void manageSecurityLogs() {
        Stage logsStage = new Stage();
        new SecurityLogsWindow().start(logsStage);
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