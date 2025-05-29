package Server.Models;

import java.sql.Timestamp;

/**
 * Модель для представления лога безопасности.
 */
public class SecurityLog {
    private int id;
    private String username;
    private String action;
    private Timestamp timestamp;
    private String details;

    /**
     * Конструктор для создания лога.
     *
     * @param id        Идентификатор лога
     * @param username  Имя пользователя
     * @param action    Действие
     * @param timestamp Временная метка
     * @param details   Детали действия
     */
    public SecurityLog(int id, String username, String action, Timestamp timestamp, String details) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

    /**
     * Возвращает строковое представление лога в формате, ожидаемом клиентом.
     *
     * @return Строка в формате id,username,action,timestamp,details
     */
    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s",
                id,
                username,
                action,
                timestamp,
                details != null ? details : "");
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }
}