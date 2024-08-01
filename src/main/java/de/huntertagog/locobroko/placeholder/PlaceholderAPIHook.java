package de.huntertagog.locobroko.placeholder;

import de.huntertagog.locobroko.CoinSystem;
import de.huntertagog.locobroko.manager.CoinManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    FileConfiguration config = CoinSystem.getInstance().getConfig();
    private final String prefix;
    private final CoinManager coinManager;

    public PlaceholderAPIHook(CoinManager coinManager) {
        this.prefix = " " + config.getString("prefix");
        this.coinManager = coinManager;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "HunterTagOG";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "locobrokostars";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {

        if (offlinePlayer != null && offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) return null;

            if (params.equalsIgnoreCase("balance")) {
                return coinManager.getCoins(player.getUniqueId()) + prefix;
            }
        }
        return null;
    }
}
