package de.huntertagog.locobroko.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles database operations for managing player coins and logs.
 */
public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private final HikariDataSource dataSource;

    /**
     * Initializes the Database object with the given connection parameters.
     * Creates the required tables if they do not exist.
     *
     * @param url      the JDBC URL
     * @param user     the database user
     * @param password the database password
     */
    public Database(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);

        this.dataSource = new HikariDataSource(config);
        createTablesIfNotExists();
    }

    /**
     * Gets a connection from the HikariCP connection pool.
     *
     * @return a database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Creates the required tables if they do not exist.
     */
    private void createTablesIfNotExists() {
        String createPlayerCoinsTableSQL = "CREATE TABLE IF NOT EXISTS player_coins (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "coins INT NOT NULL" +
                ")";
        String createLogsTableSQL = "CREATE TABLE IF NOT EXISTS player_logs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "uuid VARCHAR(36) NOT NULL," +
                "action VARCHAR(255) NOT NULL," +
                "amount INT NOT NULL," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createPlayerCoinsTableSQL);
            stmt.execute(createLogsTableSQL);
            logger.info("Tables player_coins and player_logs checked/created successfully.");
        } catch (SQLException e) {
            logger.error("Failed to create tables.", e);
        }
    }

    /**
     * Checks if a player exists in the database.
     *
     * @param playerUUID the UUID of the player
     * @return true if the player exists, false otherwise
     */
    public boolean playerExists(UUID playerUUID) {
        String query = "SELECT 1 FROM player_coins WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error checking if player exists {}", playerUUID, e);
        }
        return false;
    }

    /**
     * Gets the number of coins for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @return the number of coins the player has
     */
    public int getCoins(UUID playerUUID) {
        String query = "SELECT coins FROM player_coins WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("coins");
            }
        } catch (SQLException e) {
            logger.error("Error getting coins for player {}", playerUUID, e);
        }
        return 0; // Default value if no data is found
    }

    /**
     * Updates the number of coins for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @param coins      the new number of coins
     */
    public void updateCoins(UUID playerUUID, int coins) {
        String query = "INSERT INTO player_coins (uuid, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setInt(2, coins);
            pstmt.setInt(3, coins);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating coins for player {}", playerUUID, e);
        }
    }

    /**
     * Logs a transaction for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @param action     the action performed
     * @param amount     the amount involved in the transaction
     */
    public void logTransaction(UUID playerUUID, String action, int amount) {
        String query = "INSERT INTO player_logs (uuid, action, amount) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerUUID != null ? playerUUID.toString() : "all");
            pstmt.setString(2, action);
            pstmt.setInt(3, amount);
            pstmt.executeUpdate();
            logger.info("Logged transaction for player {}: {} {}", playerUUID, action, amount);
        } catch (SQLException e) {
            logger.error("Error logging transaction for player {}", playerUUID, e);
        }
    }

    /**
     * Gets the transaction logs for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @return a ResultSet containing the logs
     */
    public ResultSet getPlayerLogs(UUID playerUUID) {
        String query = "SELECT action, amount, timestamp FROM player_logs WHERE uuid = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, playerUUID.toString());
            return pstmt.executeQuery();
        } catch (SQLException e) {
            logger.error("Error getting logs for player {}", playerUUID, e);
        }
        return null;
    }

    /**
     * Exports and deletes logs older than one week.
     *
     * @param baseExportPath the base path for exporting the logs
     */
    public void exportAndDeleteOldLogs(String baseExportPath) {
        String query = "SELECT * FROM player_logs WHERE timestamp < NOW() - INTERVAL 1 WEEK";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            Map<UUID, FileWriter> writers = new HashMap<>();
            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("uuid"));
                File playerDir = new File(baseExportPath, playerUUID.toString());
                if (!playerDir.exists()) {
                    playerDir.mkdirs();
                }
                File logFile = new File(playerDir, "player_logs_" + System.currentTimeMillis() + ".csv");

                FileWriter writer = writers.computeIfAbsent(playerUUID, uuid -> {
                    try {
                        return new FileWriter(logFile, true);
                    } catch (IOException e) {
                        logger.error("Failed to create FileWriter for UUID: {}", uuid, e);
                        return null;
                    }
                });

                if (writer != null) {
                    String logEntry = String.format("%s,%s,%d,%s\n",
                            rs.getString("uuid"),
                            rs.getString("action"),
                            rs.getInt("amount"),
                            rs.getTimestamp("timestamp").toString());
                    writer.write(logEntry);
                }
            }

            for (FileWriter writer : writers.values()) {
                if (writer != null) {
                    writer.close();
                }
            }

            String deleteQuery = "DELETE FROM player_logs WHERE timestamp < NOW() - INTERVAL 1 WEEK";
            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteQuery)) {
                deletePstmt.executeUpdate();
                logger.info("Exported and deleted old logs successfully.");
            }

        } catch (SQLException | IOException e) {
            logger.error("Error exporting and deleting old logs.", e);
        }
    }

    /**
     * Deletes the logs for a specific player.
     *
     * @param playerUUID the UUID of the player
     */
    public void deletePlayerLogs(UUID playerUUID) {
        String query = "DELETE FROM player_logs WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting logs for player {}: {}", playerUUID, e.getMessage(), e);
        }
    }

    /**
     * Deletes all logs.
     */
    public void deleteAllLogs() {
        String query = "DELETE FROM player_logs";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting all logs: {}", e.getMessage(), e);
        }
    }

    /**
     * Closes the HikariCP connection pool.
     */
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
