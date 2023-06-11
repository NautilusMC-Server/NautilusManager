package org.nautilusmc.nautilusmanager.teleport.commands.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Warps;

import java.util.ArrayList;
import java.util.List;

public class DelWarpCommand extends Command {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.CREATE_WARPS)) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        if (strings.length < 1) return false;

        Location warp = Warps.getWarp(strings[0]);
        if (warp == null) {
            player.sendMessage(Component.text("Could not find a warp with the name \"")
                    .append(Component.text(strings[0]).color(Default.ERROR_ACCENT_COLOR))
                    .append(Component.text("\"!"))
                    .color(Default.ERROR_COLOR));
            return true;
        }

        Warps.removeWarp(strings[0]);
        player.sendMessage(Component.text("Warp \"")
                .append(Component.text(strings[0]).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("\" has been deleted."))
                .color(Default.INFO_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (!(commandSender instanceof Player)) return out;

        if (strings.length == 1) {
            out.addAll(Warps.getWarps());
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
