package com.bloon.anticheat;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private final Main plugin;
    private Connection connection;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "bloonac.db");
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());

            String sql = "CREATE TABLE IF NOT EXISTS sanctions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "uuid TEXT NOT NULL,"
                    + "player_name TEXT NOT NULL,"
                    + "type TEXT NOT NULL,"
                    + "reason TEXT NOT NULL,"
                    + "duration TEXT NOT NULL,"
                    + "ip TEXT"
                    + ");";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.execute();
            }

            plugin.getLogger().info("[BloonAC] Base de datos SQLite inicializada correctamente.");
        } catch (Exception e) {
            plugin.getLogger().severe("[BloonAC] Error inicializando SQLite: " + e.getMessage());
        }
    }

    public void saveSanction(String uuid, String name, String type, String reason, String duration, String ip) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO sanctions(uuid, player_name, type, reason, duration, ip) VALUES(?,?,?,?,?,?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid);
                stmt.setString(2, name);
                stmt.setString(3, type);
                stmt.setString(4, reason);
                stmt.setString(5, duration);
                stmt.setString(6, ip);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
