package de.huntertagog.locobroko.api;

import java.util.UUID;

/**
 * Interface for managing coins of players.
 */
public interface CoinAPI {

    /**
     * Gets the number of coins for a specific player.
     *
     * @param playerUUID the UUID of the player
     * @return the number of coins the player has
     */
    int getCoins(UUID playerUUID);

    /**
     * Adds a specified amount of coins to a player's balance.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to add
     */
    void addCoins(UUID playerUUID, int amount);

    /**
     * Removes a specified amount of coins from a player's balance.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to remove
     */
    void removeCoins(UUID playerUUID, int amount);

    /**
     * Sets a player's balance to a specified amount of coins.
     *
     * @param playerUUID the UUID of the player
     * @param amount the amount of coins to set
     */
    void setCoins(UUID playerUUID, int amount);
}
