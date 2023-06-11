package org.nautilusmc.nautilusmanager.teleport.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;

public class SpawnCommand extends Command {
    public static final Component NO_SPAWN_SET_ERROR = Component.text("Spawn has not been set!").color(ERROR_COLOR);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        Location spawn = NautilusManager.INSTANCE.getConfig().getLocation("spawn.tpLocation");
        if (spawn == null) {
            commandSender.sendMessage(NO_SPAWN_SET_ERROR);
            return true;
        }

        TeleportHandler.teleportAfterDelay(player, spawn);
        return true;
    }
}
