package de.huntertagog.locobroko.listener;

import de.huntertagog.locobroko.manager.CoinManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener class for handling player join and quit events.
 */
public class PlayerListener implements Listener {
    private final CoinManager coinManager;

    /**
     *
     * Initializes the PlayerListener with the given plugin instance.
     *
     */
    public PlayerListener() {
        this.coinManager = CoinManager.getInstance();
    }

    /**
     * Event handler for when a player joins the server.
     * Loads the player's coin balance from the database.
     *
     * @param event the PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!coinManager.getCoinBalance().containsKey(event.getPlayer().getUniqueId())) {
            coinManager.loadPlayer(event.getPlayer().getUniqueId());
            if (!coinManager.getCoinBalance().containsKey(event.getPlayer().getUniqueId())) {
                coinManager.setCoins(event.getPlayer().getUniqueId(), coinManager.getStartingBalance());
            }
        }
    }

    /**
     * Event handler for when a player quits the server.
     * Saves the player's coin balance to the database.
     *
     * @param event the PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        coinManager.savePlayer(event.getPlayer().getUniqueId());
    }
}
