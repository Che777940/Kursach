package Server.Controllers;

import Server.Models.SecurityLog;
import Server.Services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для управления логами безопасности.
 * Предоставляет методы для добавления, получения и очистки логов.
 */
public class SecurityLogsController {
    private final DatabaseService databaseService;

    /**
     * Конструктор для инициализации контроллера.
     *
     * @param databaseService Сервис для работы с базой данных
     */
    public SecurityLogsController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Добавляет новый лог в таблицу security_logs.
     *
     * @param username Имя пользователя, совершившего действие
     * @param action   Тип действия (например, LOGIN, REGISTER)
     * @param details  Дополнительные детали действия
     */
    public void addLog(String username, String action, String details) {
        String query = "INSERT INTO security_logs (username, action, timestamp, details) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, action);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, details);
            stmt.executeUpdate();
            System.out.println("Записано действие: " + action + " для пользователя: " + username);
        } catch (SQLException e) {
            System.err.println("Ошибка записи лога: " + e.getMessage());
        }
    }

    /**
     * Получает список логов с опциональными фильтрами по имени пользователя и действию.
     *
     * @param userFilter   Фильтр по имени пользователя (может быть null)
     * @param actionFilter Фильтр по действию (может быть null)
     * @return Список логов безопасности
     */
    public List<SecurityLog> getSecurityLogs(String userFilter, String actionFilter) {
        List<SecurityLog> logs = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM security_logs WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (userFilter != null && !userFilter.isEmpty()) {
            query.append(" AND username LIKE ?");
            params.add("%" + userFilter + "%");
        }
        if (actionFilter != null && !actionFilter.isEmpty()) {
            query.append(" AND action LIKE ?");
            params.add("%" + actionFilter + "%");
        }
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new SecurityLog(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("details")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения логов: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Удаляет логи старше указанного количества дней.
     *
     * @param days Количество дней
     * @return Результат операции (OK или ERROR с описанием)
     */
    public String clearOldLogs(int days) {
        String query = "DELETE FROM security_logs WHERE timestamp < NOW() - INTERVAL ? DAY";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, days);
            int rowsAffected = stmt.executeUpdate();
            return "OK: Удалено " + rowsAffected + " логов";
        } catch (SQLException e) {
            return "ERROR: Ошибка очистки логов: " + e.getMessage();
        }
    }
}