package Server.Controllers;

import Server.Models.Client;
import Server.Models.FicoParameter;
import Server.Models.FinancialInfo;
import Server.Services.DatabaseService;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinancialController {
    private final DatabaseService dbService;
    private final FicoParameterController ficoParameterController;

    public FinancialController(DatabaseService dbService, FicoParameterController ficoParameterController) {
        this.dbService = dbService;
        this.ficoParameterController = ficoParameterController;
    }

    // Получить список всех клиентов
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT ID, FullName FROM clients";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Client client = new Client(rs.getInt("ID"), rs.getString("FullName"));
                clients.add(client);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении клиентов: " + e.getMessage());
        }
        return clients;
    }

    // Получить финансовую информацию по client_id
    public FinancialInfo getFinancialInfo(int clientId) {
        String query = "SELECT id, client_id, credit_balance, overdue_amount, open_credits, credit_inquiries, last_updated, " +
                "credit_limit, credit_types, first_credit_date " +
                "FROM financial_info WHERE client_id = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FinancialInfo(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getBigDecimal("credit_balance"),
                            rs.getBigDecimal("overdue_amount"),
                            rs.getInt("open_credits"),
                            rs.getInt("credit_inquiries"),
                            rs.getDate("last_updated").toLocalDate(),
                            rs.getBigDecimal("credit_limit"),
                            rs.getString("credit_types"),
                            rs.getDate("first_credit_date") != null ? rs.getDate("first_credit_date").toLocalDate() : null
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении финансовой информации: " + e.getMessage());
        }
        return null;
    }

    // Добавить финансовую информацию
    public String addFinancialInfo(int clientId, BigDecimal creditBalance, BigDecimal overdueAmount,
                                   int openCredits, int creditInquiries, LocalDate lastUpdated,
                                   BigDecimal creditLimit, String creditTypes, LocalDate firstCreditDate) {
        if (getFinancialInfo(clientId) != null) {
            return "ERROR: Финансовая информация для клиента с ID " + clientId + " уже существует";
        }

        String query = "INSERT INTO financial_info (client_id, credit_balance, overdue_amount, open_credits, " +
                "credit_inquiries, last_updated, credit_limit, credit_types, first_credit_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            stmt.setBigDecimal(2, creditBalance);
            stmt.setBigDecimal(3, overdueAmount);
            stmt.setInt(4, openCredits);
            stmt.setInt(5, creditInquiries);
            stmt.setDate(6, java.sql.Date.valueOf(lastUpdated));
            if (creditLimit != null) {
                stmt.setBigDecimal(7, creditLimit);
            } else {
                stmt.setNull(7, java.sql.Types.DECIMAL);
            }
            stmt.setString(8, creditTypes);
            if (firstCreditDate != null) {
                stmt.setDate(9, java.sql.Date.valueOf(firstCreditDate));
            } else {
                stmt.setNull(9, java.sql.Types.DATE);
            }
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Финансовая информация добавлена";
            } else {
                return "ERROR: Не удалось добавить финансовую информацию";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Обновить финансовую информацию
    public String updateFinancialInfo(int id, BigDecimal creditBalance, BigDecimal overdueAmount,
                                      int openCredits, int creditInquiries, LocalDate lastUpdated,
                                      BigDecimal creditLimit, String creditTypes, LocalDate firstCreditDate) {
        String query = "UPDATE financial_info SET credit_balance = ?, overdue_amount = ?, open_credits = ?, " +
                "credit_inquiries = ?, last_updated = ?, credit_limit = ?, credit_types = ?, first_credit_date = ? " +
                "WHERE id = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setBigDecimal(1, creditBalance);
            stmt.setBigDecimal(2, overdueAmount);
            stmt.setInt(3, openCredits);
            stmt.setInt(4, creditInquiries);
            stmt.setDate(5, java.sql.Date.valueOf(lastUpdated));
            if (creditLimit != null) {
                stmt.setBigDecimal(6, creditLimit);
            } else {
                stmt.setNull(6, java.sql.Types.DECIMAL);
            }
            stmt.setString(7, creditTypes);
            if (firstCreditDate != null) {
                stmt.setDate(8, java.sql.Date.valueOf(firstCreditDate));
            } else {
                stmt.setNull(8, java.sql.Types.DATE);
            }
            stmt.setInt(9, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Финансовая информация обновлена";
            } else {
                return "ERROR: Финансовая информация с ID " + id + " не найдена";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Удалить финансовую информацию
    public String deleteFinancialInfo(int id) {
        String query = "DELETE FROM financial_info WHERE id = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Финансовая информация удалена";
            } else {
                return "ERROR: Финансовая информация с ID " + id + " не найдена";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Проверка существования клиента
    public boolean clientExists(int clientId) {
        String query = "SELECT 1 FROM clients WHERE ID = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при проверке клиента: " + e.getMessage());
        }
    }

    // Рассчитать FICO-балл на основе финансовой информации клиента
    public int calculateFicoScoreForClient(int clientId) {
        FinancialInfo financialInfo = getFinancialInfo(clientId);
        if (financialInfo == null) {
            throw new RuntimeException("Финансовая информация для клиента " + clientId + " не найдена");
        }

        List<FicoParameter> parameters = ficoParameterController.getAllParameters();
        if (parameters.size() != 5) {
            throw new RuntimeException("Ожидается 5 параметров FICO, найдено: " + parameters.size());
        }

        int totalScore = 0;

        // Платёжная история
        int paymentHistoryScore = financialInfo.calculatePaymentHistoryScore();
        totalScore += (paymentHistoryScore * parameters.get(0).getWeightPercentage()) / 100;

        // Сумма задолженности
        int debtAmountScore = financialInfo.calculateDebtAmountScore();
        totalScore += (debtAmountScore * parameters.get(1).getWeightPercentage()) / 100;

        // Длительность кредитной истории
        int creditHistoryLengthScore = financialInfo.calculateCreditHistoryLengthScore();
        totalScore += (creditHistoryLengthScore * parameters.get(2).getWeightPercentage()) / 100;

        // Новые кредиты
        int newCreditsScore = financialInfo.calculateNewCreditsScore();
        totalScore += (newCreditsScore * parameters.get(3).getWeightPercentage()) / 100;

        // Типы кредитов
        int creditTypesScore = financialInfo.calculateCreditTypesScore();
        totalScore += (creditTypesScore * parameters.get(4).getWeightPercentage()) / 100;

        // Масштабируем балл до диапазона 300–850
        int scaledScore = 300 + (totalScore * 550 / 100);
        int finalScore = Math.min(850, Math.max(300, scaledScore));

        // Определяем статус одобрения кредита
        String creditApprovalStatus = finalScore >= 650 ? "Одобрен" : "Не одобрен";

        // Сохраняем результат в таблицу scoring_results
        saveScoringResult(clientId, finalScore, creditApprovalStatus);

        return finalScore;
    }

    // Сохранение результата скоринга в таблицу scoring_results
    private void saveScoringResult(int clientId, int ficoScore, String creditApprovalStatus) {
        String fullName = getClientFullName(clientId);
        if (fullName == null) {
            throw new RuntimeException("Клиент с ID " + clientId + " не найден");
        }

        String query = "INSERT INTO scoring_results (client_id, full_name, scoring_parameter, scoring_value, credit_approval_status, calculation_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            stmt.setString(2, fullName);
            stmt.setString(3, "FICO-балл");
            stmt.setInt(4, ficoScore);
            stmt.setString(5, creditApprovalStatus);
            stmt.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении результата скоринга: " + e.getMessage());
        }
    }

    // Получение ФИО клиента
    private String getClientFullName(int clientId) {
        String query = "SELECT FullName FROM clients WHERE ID = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("FullName");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении ФИО клиента: " + e.getMessage());
        }
        return null;
    }

    // Получение статуса одобрения для последнего расчёта
    public String getCreditApprovalStatus(int clientId, int ficoScore) {
        String query = "SELECT credit_approval_status FROM scoring_results " +
                "WHERE client_id = ? AND scoring_value = ? " +
                "ORDER BY calculation_date DESC LIMIT 1";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, ficoScore);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("credit_approval_status");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении статуса одобрения: " + e.getMessage());
        }
        throw new RuntimeException("Статус одобрения для клиента " + clientId + " не найден");
    }


}