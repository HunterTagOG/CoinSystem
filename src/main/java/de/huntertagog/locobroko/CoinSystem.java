package de.huntertagog.locobroko;

import de.huntertagog.locobroko.api.CoinAPI;
import de.huntertagog.locobroko.api.ICoinAPI;
import de.huntertagog.locobroko.api.CoinAPIImpl;
import de.huntertagog.locobroko.listener.PlayerListener;
import de.huntertagog.locobroko.manager.CoinManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.bukkit.plugin.ServicePriority;

import java.util.UUID;

@Getter
public final class CoinSystem extends SimplePlugin {

    private static CoinSystem instance;
    private CoinManager coinManager;
    @Getter
    private CoinAPIImpl coinAPI;

    /**
     * Called when the plugin is first enabled. Initializes configuration, registers API, events,
     * schedules regular backups, and schedules log export and deletion.
     */
    @Override
    public void onPluginStart() {
        // Plugin startup logic
        this.saveDefaultConfig();
        coinManager = new CoinManager(getInstance());

        // Register the API
        coinAPI = new CoinAPIImpl(coinManager);
        CoinAPI.setApi(coinAPI);
        Bukkit.getServicesManager().register(ICoinAPI.class, coinAPI, this, ServicePriority.Normal);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Schedule regular backups (e.g., every 30 minutes)
        long backupInterval = getConfig().getInt("backup_interval_minutes", 30) * 60 * 20L; // Convert minutes to ticks
        coinManager.scheduleRegularBackups(backupInterval);

        // Schedule regular export and deletion of old logs (e.g., daily)
        scheduleLogExportAndDeletion();

        getLogger().info("@ CoinSystem has been enabled!");
    }

    /**
     * Called when the plugin is disabled. Saves player data, performs backups,
     * closes the database connection, and unregisters all listeners.
     */
    @Override
    public void onPluginStop() {
        // Plugin shutdown logic
        // Save all data when the plugin stops
        for (UUID playerUUID : coinManager.getCoinBalance().keySet()) {
            coinManager.savePlayer(playerUUID);
        }
        coinManager.backupData();
        coinManager.saveAllPlayers();

        // Close the database connection
        coinManager.getDatabase().close();
        HandlerList.unregisterAll((JavaPlugin) this); // Unregister all listeners for safe reload

        getLogger().info("@ CoinSystem has been disabled!");
    }

    /**
     * Called when the plugin is loaded. Use this to initialize any data structures or configurations
     * that need to be available before the plugin is enabled.
     */
    @Override
    protected void onPluginLoad() {
        // Load logic if needed
    }

    @Override
    protected void onPluginReload() {
        // Plugin reload logic
        instance = null;
        // Save all data when the plugin stops
        for (UUID playerUUID : coinManager.getCoinBalance().keySet()) {
            coinManager.savePlayer(playerUUID);
        }
        coinManager.backupData();
        coinManager.saveAllPlayers();

        // Close the database connection
        coinManager.getDatabase().close();
        HandlerList.unregisterAll((JavaPlugin) this); // Unregister all listeners for safe reload

        getLogger().info("@ CoinSystem has been reloaded!");
    }

    /**
     * Gets the instance of the CoinSystem plugin.
     *
     * @return the instance of CoinSystem
     */
    public static CoinSystem getInstance() {
        return (CoinSystem) SimplePlugin.getInstance();
    }

    /**
     * Schedules a task to regularly export and delete old logs.
     */
    private void scheduleLogExportAndDeletion() {
        if (!getConfig().getBoolean("logging_enabled", true)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                String baseExportPath = getDataFolder() + "/logs";
                coinManager.getDatabase().exportAndDeleteOldLogs(baseExportPath);
            }
        }.runTaskTimer(this, 0, 20 * 60 * 60 * 24); // Every 24 hours
    }
}
