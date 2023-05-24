package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;

public class DenyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text("You must be a player to use this command").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        Player player = (Player) commandSender;
        if (!player.hasPermission(NautilusCommand.DENY_PERM)) {
            NautilusCommand.error(player, NautilusCommand.DEFAULT_PERM_MESSAGE);
            return true;
        }
        ConfirmationMessage.deny(player);
        return true;
    }
}
