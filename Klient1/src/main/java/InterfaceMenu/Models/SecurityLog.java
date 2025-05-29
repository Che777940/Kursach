package InterfaceMenu.Models;

import java.sql.Timestamp;

public class SecurityLog {
    private int id;
    private String username;
    private String action;
    private Timestamp timestamp;
    private String details;

    public SecurityLog(int id, String username, String action, Timestamp timestamp, String details) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

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

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s",
                id,
                username,
                action,
                timestamp,
                details != null ? details : "");
    }
}