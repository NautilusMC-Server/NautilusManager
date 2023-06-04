package org.nautilusmc.nautilusmanager.teleport.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;

public class BackCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        Location lastLoc = TeleportHandler.getLastTeleportLocation(player);
        if (lastLoc == null) {
            commandSender.sendMessage(ErrorMessage.NO_PREV_LOCATION);
            return true;
        }

        TeleportHandler.teleportAfterDelay(player, lastLoc);

        return true;
    }
}
