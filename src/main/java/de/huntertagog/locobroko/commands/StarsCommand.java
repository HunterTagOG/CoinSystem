package de.huntertagog.locobroko.commands;

import de.huntertagog.locobroko.CoinSystem;
import de.huntertagog.locobroko.manager.CoinManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoRegister
public final class StarsCommand extends SimpleCommand {

    private final CoinManager coinManager;
    private final String prefix;
    FileConfiguration config = CoinSystem.getInstance().getConfig();

    /**
     *
     * Initializes the StarsCommand with the given plugin instance.
     * Sets the command usage and description.
     *
     */
    public StarsCommand() {
        super("stars|s");
        this.setUsage("/stars <subcommand> <player> <amount>");
        this.setDescription("Stars Command");
        this.coinManager = CoinManager.getInstance();
        this.setMinArguments(0);
        this.prefix = " " + config.getString("prefix");
    }

    /**
     * Executes the command logic.
     * Checks for permissions and the validity of arguments, then performs the appropriate action.
     */
    @Override
    protected void onCommand() {
        this.checkConsole();

        if (args.length == 0) {
            if (sender instanceof Player player) {
                int coins = coinManager.getCoins(player.getUniqueId());
                player.sendMessage(Component.text("You have " + coins + prefix).color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            }
            return;
        }

        if (args.length >= 2) {
            Player target = this.findPlayer(args[1]);
            if (args[0].equalsIgnoreCase("see") && args.length == 2) {
                if (!sender.hasPermission("coinsystem.admin") || !sender.isOp()) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                    return;
                }

                if (target != null) {
                    int coins = coinManager.getCoins(target.getUniqueId());
                    sender.sendMessage(Component.text(target.getName() + " has " + coins + prefix).color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                }
                return;
            }

            if (args[0].equalsIgnoreCase("set") && args.length == 3) {
                if (!sender.hasPermission("coinsystem.admin") || !sender.isOp()) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                    return;
                }

                if (target != null) {
                    try {
                        int amount = Integer.parseInt(args[2]);
                        coinManager.setCoins(target.getUniqueId(), amount);
                        sender.sendMessage(Component.text("Set " + target.getName() + "'s" + prefix + " to " + amount + ".").color(NamedTextColor.GREEN));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid number format.").color(NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                }
                return;
            }

            if (args[0].equalsIgnoreCase("reset") && args.length == 2) {
                if (!sender.hasPermission("coinsystem.admin") || !sender.isOp()) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                    return;
                }

                if (target != null) {
                    coinManager.setCoins(target.getUniqueId(), coinManager.getStartingBalance());
                    sender.sendMessage(Component.text("Reset " + target.getName() + "'s" + prefix).color(NamedTextColor.GREEN));
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
     * Suggests subcommands, player names, and coin amounts.
     *
     * @return a list of suggested completions
     */
    @Override
    protected List<String> tabComplete() {
        if (args.length == 1) {
            return Stream.of("see", "set", "reset")
                    .filter(subcommand -> subcommand.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("see") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset")) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames.stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("10", "100", "1000");
        }

        return null;
    }
}
