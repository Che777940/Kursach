package Server.Services;

import Server.Config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService implements AutoCloseable {
    private Connection connection;

    public DatabaseService() throws SQLException {
        this.connection = DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        );
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии соединения с БД: " + e.getMessage());
        }
    }
}
