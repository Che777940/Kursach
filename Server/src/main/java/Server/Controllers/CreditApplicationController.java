package Server.Controllers;

import Server.Models.CreditApplication;
import Server.Services.DatabaseService;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreditApplicationController {
    private final DatabaseService dbService;

    public CreditApplicationController(DatabaseService dbService) {
        this.dbService = dbService;
    }

    // Получить все заявки
    public List<CreditApplication> getAllApplications() {
        List<CreditApplication> applications = new ArrayList<>();
        String query = "SELECT id, client_id, loan_amount, loan_term, purpose, status, submission_date " +
                "FROM credit_applications";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(new CreditApplication(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getBigDecimal("loan_amount"),
                            rs.getInt("loan_term"),
                            rs.getString("purpose"),
                            rs.getString("status"),
                            rs.getDate("submission_date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех заявок: " + e.getMessage());
        }
        return applications;
    }

    // Получить все заявки по client_id
    public List<CreditApplication> getApplicationsByClientId(int clientId) {
        List<CreditApplication> applications = new ArrayList<>();
        String query = "SELECT id, client_id, loan_amount, loan_term, purpose, status, submission_date " +
                "FROM credit_applications WHERE client_id = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(new CreditApplication(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getBigDecimal("loan_amount"),
                            rs.getInt("loan_term"),
                            rs.getString("purpose"),
                            rs.getString("status"),
                            rs.getDate("submission_date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении заявок: " + e.getMessage());
        }
        return applications;
    }

    // Добавить заявку
    public String addCreditApplication(int clientId, BigDecimal loanAmount, int loanTerm, String purpose, LocalDate submissionDate) {
        String query = "INSERT INTO credit_applications (client_id, loan_amount, loan_term, purpose, submission_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, clientId);
            stmt.setBigDecimal(2, loanAmount);
            stmt.setInt(3, loanTerm);
            stmt.setString(4, purpose);
            stmt.setDate(5, java.sql.Date.valueOf(submissionDate));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Заявка на кредит добавлена";
            } else {
                return "ERROR: Не удалось добавить заявку";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Обновить статус заявки
    public String updateApplicationStatus(int id, String status) {
        String query = "UPDATE credit_applications SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Статус заявки обновлен";
            } else {
                return "ERROR: Заявка с ID " + id + " не найдена";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Удалить заявку
    public String deleteCreditApplication(int id) {
        String query = "DELETE FROM credit_applications WHERE id = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Заявка удалена";
            } else {
                return "ERROR: Заявка с ID " + id + " не найдена";
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
}