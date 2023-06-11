package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Permission;

public class TpCancelCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.TPA.toString())) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        TpaManager.cancelRequest(player);

        return true;
    }
}
