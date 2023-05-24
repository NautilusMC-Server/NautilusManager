package org.nautilusmc.nautilusmanager.teleport.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        Location spawn = NautilusManager.INSTANCE.getConfig().getLocation("spawn.tpLocation");
        if (spawn == null) {
            commandSender.sendMessage(Component.text("Spawn has not been set.").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        TeleportHandler.teleportAfterDelay(player, spawn, 100);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
