package Server.Controllers;

import Server.Models.User;
import Server.Services.DatabaseService;

import java.sql.*;

public class AuthController {
    private final DatabaseService dbService;

    public AuthController(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public String login(String username, String password) {
        String query = "SELECT password, role FROM users WHERE username = ?";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String role = rs.getString("role");
                    if (storedPassword.equals(password)) {
                        return "OK " + role;
                    } else {
                        return "ERROR: Неверный пароль!";
                    }
                } else {
                    return "ERROR: Пользователь не найден!";
                }
            }
        } catch (SQLException e) {
            return "ERROR: Ошибка сервера.";
        }
    }

    public String register(User user) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = dbService.getConnection().prepareStatement(checkQuery)) {
            checkStmt.setString(1, user.getUsername());
            try (ResultSet rs = checkStmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return "ERROR: Пользователь уже существует!";
                }
            }
        } catch (SQLException e) {
            return "ERROR: Ошибка сервера.";
        }

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(insertQuery)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.executeUpdate();
            return "OK: Регистрация успешна!";
        } catch (SQLException e) {
            return "ERROR: Ошибка сервера.";
        }
    }
}
