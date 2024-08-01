package de.huntertagog.locobroko.commands;

import de.huntertagog.locobroko.CoinSystem;
import de.huntertagog.locobroko.database.Database;
import de.huntertagog.locobroko.manager.CoinManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AutoRegister
public final class LogResetCommand extends SimpleCommand {

    private final CoinManager coinManager;

    /**
     *
     * Initializes the LogResetCommand with the given plugin instance.
     * Sets the command usage and description.
     *
     */
    public LogResetCommand() {
        super("logreset|lr");
        this.setUsage("/logreset <player|all>");
        this.setDescription("Resets the logs of a specific player or all logs.");
        this.coinManager = CoinSystem.getInstance().getCoinManager();
        this.setMinArguments(0);
    }

    /**
     * Executes the command logic.
     * Checks for permissions and the validity of arguments, then deletes the logs accordingly.
     */
    @Override
    protected void onCommand() {
        this.checkConsole();
        if (!sender.hasPermission("coinsystem.admin") || !sender.isOp()) {
            sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 1) {
            Database database = coinManager.getDatabase();
            if (args[0].equalsIgnoreCase("all")) {
                // Deletes all logs
                database.deleteAllLogs();
                sender.sendMessage(Component.text("All logs have been deleted.").color(NamedTextColor.GREEN));
                return;
            } else {
                // Deletes logs for a specific player
                Player target = this.findPlayer(args[0]);
                if (target != null) {
                    UUID playerUUID = target.getUniqueId();
                    database.deletePlayerLogs(playerUUID);
                    sender.sendMessage(Component.text("Logs for " + target.getName() + " have been deleted.").color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                }
                return;
            }
        }

        sender.sendMessage(Component.text("Invalid command usage.").color(NamedTextColor.RED));
    }

    /**
     * Provides tab completion options for the command.
     * Suggests "all" and online player names.
     *
     * @return a list of suggested completions
     */
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("all");
            for (Player player : Bukkit.getOnlinePlayers()) {
                options.add(player.getName());
            }
            return options.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
