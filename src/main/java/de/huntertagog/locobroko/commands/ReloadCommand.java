package de.huntertagog.locobroko.commands;

import de.huntertagog.locobroko.CoinSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

@AutoRegister
public final class ReloadCommand extends SimpleCommand {

    /**
     * Initializes the ReloadCommand with the given plugin instance.
     * Sets the command usage and description.
     */
    public ReloadCommand() {
        super("reload-coinsystem|rl-c");
        this.setUsage("/reload-coinsystem");
        this.setDescription("Reload the CoinSystem plugin");
        this.setMinArguments(0);
    }

    /**
     * Executes the command logic.
     * Checks for permissions and the validity of arguments, then reloads the plugin.
     */
    @Override
    protected void onCommand() {
        this.checkConsole();
        if (!sender.hasPermission("coinsystem.admin") || !sender.isOp()) {
            sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        CoinSystem.getInstance().reload();
        sender.sendMessage(Component.text("CoinSystem was successfully reloaded!").color(NamedTextColor.GREEN));
    }
}
