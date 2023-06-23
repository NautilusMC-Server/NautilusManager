package org.nautilusmc.nautilusmanager.sql;

import org.bukkit.Bukkit;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public abstract class SQLHandler {
    private final String table;
    private final String primaryKey;

    public SQLHandler(String table) {
        this(table, "uuid");
    }

    public SQLHandler(String table, String primaryKey) {
        this.table = table;
        this.primaryKey = primaryKey;

        Bukkit.getScheduler().runTaskTimer(NautilusManager.INSTANCE, () -> {
            if (!SQL.isEnabled()) return;

            try {
                Connection connection = SQL.getConnection();
                Statement statement = connection.createStatement();

                update(statement.executeQuery("SELECT * FROM %s".formatted(table)));

                connection.close();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error updating SQL database!", e);
            }
        }, 0, SQL.getUpdateIntervalSeconds() * (long) Util.TICKS_PER_SECOND);
    }

    public abstract void update(ResultSet results) throws SQLException;

    private Object sanitize(Object value) {
        return value instanceof String string ? string.replace("'", "\\'") : value;
    }

    public ResultSet getValues(UUID uuid) {
        return getValues(uuid.toString());
    }

    public ResultSet getValues(String uuid) {
        if (!SQL.isEnabled()) return null;

        try {
            Connection connection = SQL.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM %s WHERE %s='%s'".formatted(table, primaryKey, uuid));
            connection.close();
            return results;
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error retrieving from SQL database!", e);
        }

        return null;
    }

    public void setValues(UUID uuid, Map<String, Object> values) {
        setValues(uuid.toString(), values);
    }

    public void setValues(String uuid, Map<String, Object> values) {
        if (!SQL.isEnabled()) return;

        values = new HashMap<>(values);
        values.replaceAll((k, v) -> sanitize(v));

        StringBuilder command = new StringBuilder("INSERT INTO ").append(table).append(" (").append(primaryKey);

        for (String key : values.keySet()) {
            command.append(",").append(key);
        }

        command.append(") VALUES ('").append(uuid).append("'");

        for (Object value : values.values()) {
            command.append(",");
            if (value instanceof String) command.append("'").append(value).append("'");
            else command.append(value);
        }

        command.append(") ON DUPLICATE KEY UPDATE ");
        for (Map.Entry<String, Object> value : values.entrySet()) {
            command.append(value.getKey()).append("=");
            if (value.getValue() instanceof String) command.append("'").append(value.getValue()).append("',");
            else command.append(value.getValue()).append(",");
        }
        command.deleteCharAt(command.length() - 1);

        try {
            Connection connection = SQL.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(command.toString());
            connection.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error inserting into SQL database!", e);
        }
    }

    public void deleteEntry(UUID uuid) {
        deleteEntry(uuid.toString());
    }

    public void deleteEntry(String uuid) {
        if (!SQL.isEnabled()) return;

        try {
            Connection connection = SQL.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM %s WHERE %s='%s'".formatted(table, primaryKey, uuid));
            connection.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error deleting from SQL database!", e);
        }
    }
}
