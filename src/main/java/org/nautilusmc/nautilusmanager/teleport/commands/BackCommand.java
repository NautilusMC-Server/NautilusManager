package org.nautilusmc.nautilusmanager.teleport.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;

public class BackCommand extends Command {
    public static final Component NO_PREV_LOCATION_ERROR = Component.text("Nowhere to return to!").color(ERROR_COLOR);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        Location lastLoc = TeleportHandler.getLastTeleportLocation(player);
        if (lastLoc == null) {
            commandSender.sendMessage(NO_PREV_LOCATION_ERROR);
            return true;
        }

        TeleportHandler.teleportAfterDelay(player, lastLoc);

        return true;
    }
}
