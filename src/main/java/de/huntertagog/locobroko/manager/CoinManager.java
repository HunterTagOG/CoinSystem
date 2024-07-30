package de.huntertagog.locobroko.manager;

import de.huntertagog.locobroko.CoinSystem;
import de.huntertagog.locobroko.database.Database;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the coins of players.
 */
@Getter
public class CoinManager {
    private static final Logger logger = LoggerFactory.getLogger(CoinManager.class);

    private static CoinManager instance;
    private final CoinSystem plugin;
    private final Map<UUID, Integer> coinBalance;
    private final int startingBalance;
    private final Database database;
    private final boolean loggingEnabled;
    private final boolean backupEnabled;

    /**
     *
     * Initializes the CoinManager with the given plugin instance.
     * Loads the configuration and initializes the database.
     *
     */
    public CoinManager(CoinSystem plugin) {
        this.plugin = plugin;
        this.coinBalance = new HashMap<>();
        FileConfiguration config = plugin.getConfig();
        this.startingBalance = config.getInt("starting_balance");
        this.loggingEnabled = config.getBoolean("logging_enabled", true);
        this.backupEnabled = config.getBoolean("backup_enabled", true);
        this.database = new Database(
                config.getString("database.url"),
                config.getString("database.user"),
                config.getString("database.password")
        );
    }

    public static CoinManager getInstance() {
        if (instance == null) {
            instance = new CoinManager(CoinSystem.getInstance());
        }
        return instance;
    }

    /**
     * Gets the number of coins for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @return the number of coins the player has
     */
    public int getCoins(UUID playerUUID) {
        return coinBalance.getOrDefault(playerUUID, startingBalance);
    }

    /**
     * Sets the number of coins for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @param amount the number of coins to set
     */
    public void setCoins(UUID playerUUID, int amount) {
        coinBalance.put(playerUUID, amount);
        if (loggingEnabled) {
            database.logTransaction(playerUUID, "set", amount);
        }
    }

    /**
     * Adds a specified amount of coins to a player's balance.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to add
     */
    public void addCoins(UUID playerUUID, int amount) {
        int currentBalance = getCoins(playerUUID);
        coinBalance.put(playerUUID, currentBalance + amount);
        if (loggingEnabled) {
            database.logTransaction(playerUUID, "add", amount);
        }
    }

    /**
     * Removes a specified amount of coins from a player's balance.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to remove
     */
    public void removeCoins(UUID playerUUID, int amount) {
        int currentBalance = getCoins(playerUUID);
        coinBalance.put(playerUUID, currentBalance - amount);
        if (loggingEnabled) {
            database.logTransaction(playerUUID, "remove", amount);
        }
    }

    /**
     * Loads a player's coin balance from the database.
     *
     * @param playerUUID the UUID of the player
     */
    public void loadPlayer(UUID playerUUID) {
        int coins = database.getCoins(playerUUID);
        if (coins != 0 || database.playerExists(playerUUID)) {
            coinBalance.put(playerUUID, coins);
        }
    }

    /**
     * Saves a player's coin balance to the database.
     *
     * @param playerUUID the UUID of the player
     */
    public void savePlayer(UUID playerUUID) {
        int coins = coinBalance.getOrDefault(playerUUID, startingBalance);
        database.updateCoins(playerUUID, coins);
        coinBalance.remove(playerUUID);
    }

    /**
     * Saves the coin balance of all players to the database.
     */
    public void saveAllPlayers() {
        for (UUID playerUUID : coinBalance.keySet()) {
            savePlayer(playerUUID);
        }
    }

    /**
     * Creates a backup of the current coin balances if backup is enabled.
     */
    public void backupData() {
        if (!backupEnabled) return;

        File backupFile = new File(plugin.getDataFolder(), "coin_backup.json");
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write("{\n");
            for (Map.Entry<UUID, Integer> entry : coinBalance.entrySet()) {
                writer.write(String.format("\"%s\": %d,\n", entry.getKey().toString(), entry.getValue()));
            }
            writer.write("}");
            logger.info("Backup saved successfully.");
        } catch (IOException e) {
            logger.error("Failed to save backup.", e);
        }
    }

    /**
     * Schedules regular backups at the specified interval if backup is enabled.
     *
     * @param interval the interval in ticks between backups
     */
    public void scheduleRegularBackups(long interval) {
        if (!backupEnabled) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                backupData();
            }
        }.runTaskTimer(plugin, interval, interval);
    }
}
