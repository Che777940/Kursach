package ScoringWindowParameters;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.image.Image;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScoringParameters extends Application {
    private static final Logger logger = Logger.getLogger(ScoringParameters.class.getName());
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static class FicoParameter {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final IntegerProperty weight = new SimpleIntegerProperty();
        private final StringProperty description = new SimpleStringProperty();

        public FicoParameter(int id, String name, int weight, String description) {
            this.id.set(id);
            this.name.set(name);
            this.weight.set(weight);
            this.description.set(description);
        }

        public IntegerProperty idProperty() { return id; }
        public StringProperty nameProperty() { return name; }
        public IntegerProperty weightProperty() { return weight; }
        public StringProperty descriptionProperty() { return description; }

        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public int getWeight() { return weight.get(); }
        public String getDescription() { return description.get(); }

        public void setWeight(int weight) { this.weight.set(weight); }
    }

    public static class ConnectionService {
        private final String host;
        private final int port;
        private static final int TIMEOUT = 5000;

        public ConnectionService(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public ServerResponse sendCommand(String command) {
            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                socket.setSoTimeout(TIMEOUT);
                out.println(command);
                logger.info("Отправлено: " + command);

                List<String> lines = new ArrayList<>();
                String line;
                boolean success = false;
                String error = null;

                while ((line = in.readLine()) != null) {
                    logger.info("Получено: " + line);
                    if (line.startsWith("OK")) {
                        success = true;
                        lines.add(line);
                        break;
                    } else if (line.startsWith("ERROR")) {
                        error = line.substring(6).trim();
                        break;
                    }
                    lines.add(line);
                }

                return new ServerResponse(success, lines, error);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Ошибка соединения: " + e.getMessage(), e);
                return new ServerResponse(false, new ArrayList<>(), "Ошибка соединения: " + e.getMessage());
            }
        }
    }

    public static class ServerResponse {
        private final boolean success;
        private final List<String> lines;
        private final String error;

        public ServerResponse(boolean success, List<String> lines, String error) {
            this.success = success;
            this.lines = lines;
            this.error = error;
        }

        public boolean isSuccess() { return success; }
        public List<String> getLines() { return lines; }
        public String getError() { return error; }
    }

    public static class ParameterManager {
        private final ConnectionService connectionService;
        private final ObservableList<FicoParameter> parameters = FXCollections.observableArrayList();
        private final IntegerProperty totalWeight = new SimpleIntegerProperty(0);

        public ParameterManager(ConnectionService connectionService) {
            this.connectionService = connectionService;
        }

        public ObservableList<FicoParameter> getParameters() { return parameters; }
        public IntegerProperty totalWeightProperty() { return totalWeight; }

        public void loadParameters() {
            ServerResponse response = connectionService.sendCommand("GET_FICO_PARAMS");
            if (response.isSuccess() && !response.getLines().isEmpty()) {
                List<FicoParameter> parsedParams = new ArrayList<>();
                String line = response.getLines().get(0);
                String[] parts = line.split("\\|");
                if (parts.length > 1 && parts[0].equals("OK")) {
                    for (int i = 1; i < parts.length; i++) {
                        Optional<FicoParameter> param = parseParameter(parts[i]);
                        param.ifPresent(parsedParams::add);
                    }
                }
                parameters.setAll(parsedParams);
                updateTotalWeight();
                if (parsedParams.isEmpty()) {
                    showError("Ошибка парсинга", "Не удалось найти валидные параметры в ответе");
                } else {
                    showInfo("Успех", "Параметры успешно загружены");
                }
            } else {
                showError("Ошибка загрузки", response.getError() != null ? response.getError() : "Параметры не получены");
            }
        }

        public void saveParameters() {
            if (!validateWeights()) {
                return;
            }
            boolean allSaved = true;
            StringBuilder errors = new StringBuilder();
            for (FicoParameter param : parameters) {
                String command = String.format("UPDATE_FICO_PARAM %d %d 0", param.getId(), param.getWeight());
                ServerResponse response = connectionService.sendCommand(command);
                if (!response.isSuccess()) {
                    allSaved = false;
                    errors.append("Ошибка сохранения ").append(param.getName())
                            .append(": ").append(response.getError()).append("\n");
                }
            }
            if (allSaved) {
                showInfo("Успех", "Параметры успешно сохранены");
                loadParameters();
            } else {
                showError("Ошибка сохранения", errors.toString());
            }
        }

        public void resetToDefaults() {
            if (confirm("Сброс к умолчанию", "Все изменения будут потеряны. Продолжить?")) {
                ServerResponse response = connectionService.sendCommand("RESET_FICO_DEFAULTS");
                if (response.isSuccess()) {
                    loadParameters();
                } else {
                    showError("Ошибка сброса", response.getError());
                }
            }
        }

        private boolean validateWeights() {
            int totalWeight = parameters.stream().mapToInt(FicoParameter::getWeight).sum();
            if (totalWeight != 100) {
                showError("Ошибка валидации",
                        String.format("Сумма весов должна быть 100%%. Текущая сумма: %d%%", totalWeight));
                return false;
            }
            for (FicoParameter param : parameters) {
                if (param.getWeight() < 0) {
                    showError("Ошибка валидации",
                            String.format("Вес параметра '%s' не может быть отрицательным", param.getName()));
                    return false;
                }
            }
            return true;
        }

        public void updateTotalWeight() {
            int sum = parameters.stream().mapToInt(FicoParameter::getWeight).sum();
            totalWeight.set(sum);
        }

        private Optional<FicoParameter> parseParameter(String paramSegment) {
            try {
                String[] paramParts = paramSegment.split(":");
                if (paramParts.length >= 4) {
                    int weight = Integer.parseInt(paramParts[2]);
                    return Optional.of(new FicoParameter(
                            Integer.parseInt(paramParts[0]),
                            paramParts[1],
                            weight,
                            paramParts[3]
                    ));
                }
            } catch (Exception e) {
                showError("Ошибка парсинга", "Не удалось разобрать параметр: " + paramSegment);
            }
            return Optional.empty();
        }

        private void showError(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private void showInfo(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private boolean confirm(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            return alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).isPresent();
        }
    }

    public static class MainView {
        private final ParameterManager parameterManager;
        private final VBox root;
        private Label totalWeightLabel;

        public MainView(ParameterManager parameterManager) {
            this.parameterManager = parameterManager;
            this.root = createView();
        }

        public VBox getView() {
            return root;
        }

        private VBox createView() {
            VBox layout = new VBox(20);
            layout.setPadding(new Insets(30));

            Label titleLabel = new Label("===== ПАРАМЕТРЫ FICO-СКОРИНГА =====");
            titleLabel.setStyle(
                    "-fx-font-family: 'Arial'; " +
                            "-fx-font-size: 24px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #ffffff; " +
                            "-fx-padding: 15px;"
            );

            TableView<FicoParameter> table = createParameterTable();
            table.setItems(parameterManager.getParameters());

            totalWeightLabel = new Label("Сумма весов: 0%");
            totalWeightLabel.setStyle(
                    "-fx-font-family: 'Arial'; " +
                            "-fx-font-size: 16px; " +
                            "-fx-text-fill: #ffffff;"
            );
            parameterManager.totalWeightProperty().addListener((obs, oldVal, newVal) -> {
                totalWeightLabel.setText("Сумма весов: " + newVal + "%");
                totalWeightLabel.setStyle(newVal.intValue() == 100 ?
                        "-fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-text-fill: #4dff4d;" :
                        "-fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-text-fill: #ff4d4d;");
            });

            HBox buttonBox = createButtonBox();
            buttonBox.setAlignment(Pos.CENTER);

            layout.getChildren().addAll(titleLabel, table, totalWeightLabel, buttonBox);
            layout.setAlignment(Pos.CENTER);
            return layout;
        }

        private TableView<FicoParameter> createParameterTable() {
            TableView<FicoParameter> table = new TableView<>();
            table.setEditable(true);
            table.setStyle(
                    "-fx-background-color: #ffffff; " +
                            "-fx-border-color: #1a3c6d; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 10px;"
            );

            TableColumn<FicoParameter, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
            idCol.setVisible(false);

            TableColumn<FicoParameter, String> nameCol = new TableColumn<>("Параметр");
            nameCol.setCellValueFactory(cell -> cell.getValue().nameProperty());
            nameCol.setMinWidth(150);
            nameCol.setStyle("-fx-alignment: CENTER;");

            TableColumn<FicoParameter, Integer> weightCol = new TableColumn<>("Вес (%)");
            weightCol.setCellValueFactory(cell -> cell.getValue().weightProperty().asObject());
            weightCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            weightCol.setOnEditCommit(event -> {
                FicoParameter param = event.getRowValue();
                int newWeight = event.getNewValue();
                if (newWeight < 0) {
                    newWeight = 0;
                    showError("Ошибка", "Вес не может быть отрицательным");
                }
                param.setWeight(newWeight);
                parameterManager.updateTotalWeight();
                table.refresh();
            });
            weightCol.setMinWidth(80);
            weightCol.setStyle("-fx-alignment: CENTER;");

            TableColumn<FicoParameter, String> descCol = new TableColumn<>("Описание");
            descCol.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
            descCol.setMinWidth(250);
            descCol.setStyle("-fx-alignment: CENTER;");

            table.getColumns().addAll(idCol, nameCol, weightCol, descCol);
            return table;
        }

        private HBox createButtonBox() {
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

            Button saveButton = new Button("Сохранить");
            saveButton.setStyle(buttonStyle);
            saveButton.setOnMouseEntered(e -> saveButton.setStyle(buttonStyle + buttonHoverStyle));
            saveButton.setOnMouseExited(e -> saveButton.setStyle(buttonStyle));
            saveButton.setOnAction(e -> parameterManager.saveParameters());

            Button resetButton = new Button("Сбросить к умолчанию");
            resetButton.setStyle(buttonStyle);
            resetButton.setOnMouseEntered(e -> resetButton.setStyle(buttonStyle + buttonHoverStyle));
            resetButton.setOnMouseExited(e -> resetButton.setStyle(buttonStyle));
            resetButton.setOnAction(e -> parameterManager.resetToDefaults());

            Button backButton = new Button("Назад");
            backButton.setStyle(buttonStyle);
            backButton.setOnMouseEntered(e -> backButton.setStyle(buttonStyle + buttonHoverStyle));
            backButton.setOnMouseExited(e -> backButton.setStyle(buttonStyle));
            backButton.setOnAction(e -> {
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.close();
                new InterfaceMenu.AdminMain().start(new Stage());
            });

            return new HBox(10, saveButton, resetButton, backButton);
        }

        private void showError(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Параметры FICO-скоринга");

        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/FICO.png")));
        } catch (Exception e) {
            System.out.println("Не удалось загрузить иконку окна: " + e.getMessage());
        }

        primaryStage.setMaximized(true);

        ConnectionService connectionService = new ConnectionService(SERVER_HOST, SERVER_PORT);
        ParameterManager parameterManager = new ParameterManager(connectionService);
        MainView mainView = new MainView(parameterManager);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a3c6d;");
        root.getChildren().add(mainView.getView());

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        parameterManager.loadParameters();
    }

    public static void main(String[] args) {
        launch(args);
    }
}