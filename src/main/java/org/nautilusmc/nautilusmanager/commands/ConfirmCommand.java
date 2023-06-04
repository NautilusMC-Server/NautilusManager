package org.nautilusmc.nautilusmanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;

public class ConfirmCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        if (!player.hasPermission(Permission.CONFIRM)) {
            player.sendMessage(ErrorMessage.NO_PERMISSION);
            return true;
        }

        ConfirmationMessage.confirm(player);
        return true;
    }
}
