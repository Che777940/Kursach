package Server.Controllers;

import Server.Models.FicoParameter;
import Server.Services.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FicoParameterController {
    private final DatabaseService dbService;

    public FicoParameterController(DatabaseService dbService) {
        this.dbService = dbService;
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        if (getAllParameters().isEmpty()) {
            addDefaultParameters();
        }
    }

    private void addDefaultParameters() {
        List<FicoParameter> defaultParams = List.of(
                new FicoParameter("Платежная история", 35,
                        "850 * 35 / 100", "История своевременности платежей"),
                new FicoParameter("Сумма задолженности", 30,
                        "850 * 30 / 100", "Общая сумма текущих задолженностей"),
                new FicoParameter("Длительность кредитной истории", 15,
                        "850 * 15 / 100", "Как долго у вас есть кредитные счета"),
                new FicoParameter("Новые кредиты", 10,
                        "850 * 10 / 100", "Количество недавно открытых кредитов"),
                new FicoParameter("Типы кредитов", 10,
                        "850 * 10 / 100", "Разнообразие типов кредитных продуктов")
        );

        for (FicoParameter param : defaultParams) {
            addParameter(param);
        }
    }

    public String addParameter(FicoParameter parameter) {
        String query = "INSERT INTO fico_parameters (parameter_name, weight_percentage, " +
                "calculation_formula, description) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, parameter.getParameterName());
            stmt.setInt(2, parameter.getWeightPercentage());
            stmt.setString(3, parameter.getCalculationFormula());
            stmt.setString(4, parameter.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        parameter.setId(generatedKeys.getInt(1));
                        return "OK: Параметр успешно добавлен с ID " + parameter.getId();
                    }
                }
                return "OK: Параметр успешно добавлен";
            } else {
                return "ERROR: Не удалось добавить параметр";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public List<FicoParameter> getAllParameters() {
        List<FicoParameter> parameters = new ArrayList<>();
        String query = "SELECT * FROM fico_parameters ORDER BY id";

        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                parameters.add(createParameterFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parameters;
    }

    public FicoParameter getParameterById(int id) {
        String query = "SELECT * FROM fico_parameters WHERE id = ?";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createParameterFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String updateParameter(FicoParameter parameter) {
        String query = "UPDATE fico_parameters SET parameter_name=?, weight_percentage=?, " +
                "calculation_formula=?, description=?, calculated_value=? WHERE id=?";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setString(1, parameter.getParameterName());
            stmt.setInt(2, parameter.getWeightPercentage());
            stmt.setString(3, parameter.getCalculationFormula());
            stmt.setString(4, parameter.getDescription());
            if (parameter.getCalculatedValue() != null) {
                stmt.setInt(5, parameter.getCalculatedValue());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, parameter.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Параметр успешно обновлен";
            } else {
                return "ERROR: Параметр с ID " + parameter.getId() + " не найден";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String resetToDefault() {
        String query = "DELETE FROM fico_parameters";

        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(query);
            addDefaultParameters();
            return "OK: Параметры сброшены к значениям по умолчанию";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public int calculateFicoScore() {
        List<FicoParameter> parameters = getAllParameters();
        int totalScore = 0;

        for (FicoParameter param : parameters) {
            try {
                // Вычисляем значение по формуле: 850 * (weight_percentage / 100)
                int value = (int) (850 * (param.getWeightPercentage() / 100.0));
                param.setCalculatedValue(value);
                totalScore += value;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return totalScore;
    }

    private FicoParameter createParameterFromResultSet(ResultSet rs) throws SQLException {
        FicoParameter parameter = new FicoParameter();
        parameter.setId(rs.getInt("id"));
        parameter.setParameterName(rs.getString("parameter_name"));
        parameter.setWeightPercentage(rs.getInt("weight_percentage"));
        parameter.setCalculationFormula(rs.getString("calculation_formula"));
        parameter.setCalculatedValue(rs.getObject("calculated_value") != null ?
                rs.getInt("calculated_value") : null);
        parameter.setDescription(rs.getString("description"));
        return parameter;
    }
}