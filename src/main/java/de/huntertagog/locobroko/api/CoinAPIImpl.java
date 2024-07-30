package de.huntertagog.locobroko.api;

import de.huntertagog.locobroko.manager.CoinManager;

import java.util.UUID;

/**
 * Implementation of the CoinAPI interface.
 * Manages player coins using the CoinManager.
 */
public class CoinAPIImpl implements ICoinAPI {
    private final CoinManager coinManager;

    /**
     *
     * Initializes the CoinAPIImplementation with the given CoinManager.
     *
     */
    public CoinAPIImpl(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    /**
     * Gets the number of coins for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @return the number of coins the player has
     */
    @Override
    public int getCoins(UUID playerUUID) {
        return coinManager.getCoins(playerUUID);
    }

    /**
     * Adds a specified amount of coins to a player's balance.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to add
     */
    @Override
    public void addCoins(UUID playerUUID, int amount) {
        coinManager.addCoins(playerUUID, amount);
    }

    /**
     * Removes a specified amount of coins from a player's balance.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to remove
     */
    @Override
    public void removeCoins(UUID playerUUID, int amount) {
        coinManager.removeCoins(playerUUID, amount);
    }

    /**
     * Sets a player's balance to a specified amount of coins.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to set
     */
    @Override
    public void setCoins(UUID playerUUID, int amount) {
        coinManager.setCoins(playerUUID, amount);
    }
}
