package org.nautilusmc.nautilusmanager.teleport.commands.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;
import org.nautilusmc.nautilusmanager.teleport.Warps;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (!player.hasPermission(WARP_TO_WARPS_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (strings.length < 1) return false;

        Location warp = Warps.getWarp(strings[0]);
        if (warp == null) {
            player.sendMessage(Component.text("Warp not found").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        TeleportHandler.teleportAfterDelay(player, warp, 5 * 20);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();
        if (!(commandSender instanceof Player player)) return out;

        if (strings.length == 1) {
            out.addAll(Warps.getWarps());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
