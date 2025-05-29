package Server.Controllers;

import Server.Models.Client;
import Server.Services.DatabaseService;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientController {
    private final DatabaseService dbService;

    public ClientController(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public String addClient(Client client) {
        String query = "INSERT INTO Clients (FullName, BirthDate, Gender, Phone, Email, Address, " +
                "MonthlyIncome, EmploymentStatus, Workplace, Assets, PassportNumber, " +
                "PassportIssueDate, PassportExpiryDate, PassportIssuingCountry) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setClientParameters(stmt, client);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return "OK: Клиент успешно добавлен с ID " + generatedKeys.getInt(1);
                    }
                }
                return "OK: Клиент успешно добавлен!";
            } else {
                return "ERROR: Не удалось добавить клиента";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Clients ORDER BY FullName";

        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                clients.add(createClientFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public Client getClientById(int id) {
        String query = "SELECT * FROM Clients WHERE ID = ?";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createClientFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String updateClient(Client client) {
        String query = "UPDATE Clients SET FullName=?, BirthDate=?, Gender=?, Phone=?, Email=?, " +
                "Address=?, MonthlyIncome=?, EmploymentStatus=?, Workplace=?, Assets=?, " +
                "PassportNumber=?, PassportIssueDate=?, PassportExpiryDate=?, PassportIssuingCountry=? " +
                "WHERE ID=?";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            setClientParameters(stmt, client);
            stmt.setInt(15, client.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Данные клиента успешно обновлены";
            } else {
                return "ERROR: Клиент с ID " + client.getId() + " не найден";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String deleteClient(int id) {
        String query = "DELETE FROM Clients WHERE ID = ?";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return "OK: Клиент с ID " + id + " успешно удален";
            } else {
                return "ERROR: Клиент с ID " + id + " не найден";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public List<Client> searchClients(String searchTerm) {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Clients WHERE FullName LIKE ? OR Phone LIKE ? OR Email LIKE ? OR PassportNumber LIKE ? ORDER BY FullName";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(createClientFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public List<Client> getClientsByIncomeRange(BigDecimal minIncome, BigDecimal maxIncome) {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Clients WHERE MonthlyIncome BETWEEN ? AND ? ORDER BY MonthlyIncome DESC";

        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(query)) {
            stmt.setBigDecimal(1, minIncome);
            stmt.setBigDecimal(2, maxIncome);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(createClientFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    // Вспомогательные методы
    private Client createClientFromResultSet(ResultSet rs) throws SQLException {
        Client client = new Client(
                rs.getString("FullName"),
                rs.getDate("BirthDate").toLocalDate(),
                rs.getString("Gender"),
                rs.getString("Phone"),
                rs.getString("Email"),
                rs.getString("Address"),
                rs.getBigDecimal("MonthlyIncome"),
                rs.getString("EmploymentStatus"),
                rs.getString("Workplace"),
                rs.getString("Assets"),
                rs.getString("PassportNumber"),
                rs.getDate("PassportIssueDate") != null ? rs.getDate("PassportIssueDate").toLocalDate() : null,
                rs.getDate("PassportExpiryDate") != null ? rs.getDate("PassportExpiryDate").toLocalDate() : null,
                rs.getString("PassportIssuingCountry")
        );
        client.setId(rs.getInt("ID"));
        return client;
    }

    private void setClientParameters(PreparedStatement stmt, Client client) throws SQLException {
        stmt.setString(1, client.getFullName());
        stmt.setDate(2, Date.valueOf(client.getBirthDate()));
        stmt.setString(3, client.getGender());
        stmt.setString(4, client.getPhone());
        stmt.setString(5, client.getEmail());
        stmt.setString(6, client.getAddress());
        stmt.setBigDecimal(7, client.getMonthlyIncome());
        stmt.setString(8, client.getEmploymentStatus());
        stmt.setString(9, client.getWorkplace());
        stmt.setString(10, client.getAssets());
        stmt.setString(11, client.getPassportNumber());
        stmt.setDate(12, client.getPassportIssueDate() != null ? Date.valueOf(client.getPassportIssueDate()) : null);
        stmt.setDate(13, client.getPassportExpiryDate() != null ? Date.valueOf(client.getPassportExpiryDate()) : null);
        stmt.setString(14, client.getPassportIssuingCountry());
    }
}