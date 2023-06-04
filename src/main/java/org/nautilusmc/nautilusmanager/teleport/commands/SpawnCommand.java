package org.nautilusmc.nautilusmanager.teleport.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;

public class SpawnCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        Location spawn = NautilusManager.INSTANCE.getConfig().getLocation("spawn.tpLocation");
        if (spawn == null) {
            commandSender.sendMessage(ErrorMessage.NO_SPAWN_SET);
            return true;
        }

        TeleportHandler.teleportAfterDelay(player, spawn);
        return true;
    }
}
