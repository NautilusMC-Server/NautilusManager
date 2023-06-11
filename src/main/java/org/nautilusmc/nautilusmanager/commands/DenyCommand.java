package org.nautilusmc.nautilusmanager.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.util.ConfirmationMessage;
import org.nautilusmc.nautilusmanager.util.Permission;

public class DenyCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.DENY.toString())) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        ConfirmationMessage.deny(player);
        return true;
    }
}
