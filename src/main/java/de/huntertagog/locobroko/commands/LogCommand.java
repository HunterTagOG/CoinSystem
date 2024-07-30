package de.huntertagog.locobroko.commands;

import de.huntertagog.locobroko.CoinSystem;
import de.huntertagog.locobroko.manager.CoinManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AutoRegister
public final class LogCommand extends SimpleCommand {

    private final CoinManager coinManager;

    /**
     *
     * Initializes the LogCommand with the given plugin instance.
     * Sets the command usage and description.
     *
     *
     */
    public LogCommand() {
        super("log|lg");
        this.setUsage("/log <player>");
        this.setDescription("Shows the transaction logs of a player.");
        this.coinManager = CoinManager.getInstance();
        this.setMinArguments(0);
    }

    /**
     * Executes the command logic.
     * Checks for permissions and the validity of arguments, then retrieves and displays the logs accordingly.
     */
    @Override
    protected void onCommand() {
        this.checkConsole();
        if (args.length == 1) {
            if (!sender.hasPermission("coinsystem.admin") || !sender.isOp()) {
                sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                return;
            }

            Player target = this.findPlayer(args[0]);
            if (target != null) {
                UUID playerUUID = target.getUniqueId();
                ResultSet logs = coinManager.getDatabase().getPlayerLogs(playerUUID);

                sender.sendMessage(Component.text("Transaction logs for " + target.getName() + ":").color(NamedTextColor.GREEN));
                try {
                    while (logs.next()) {
                        String action = logs.getString("action");
                        int amount = logs.getInt("amount");
                        String timestamp = logs.getTimestamp("timestamp").toString();
                        sender.sendMessage(Component.text(action + " " + amount + " coins at " + timestamp).color(NamedTextColor.WHITE));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(Component.text("Error retrieving logs.").color(NamedTextColor.RED));
                }
            } else {
                sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
            }
            return;
        }

        sender.sendMessage(Component.text("Invalid command usage.").color(NamedTextColor.RED));
    }

    /**
     * Provides tab completion options for the command.
     * Suggests online player names.
     *
     * @return a list of suggested completions
     */
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
