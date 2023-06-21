package org.nautilusmc.nautilusmanager.teleport.commands.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Warps;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.ArrayList;
import java.util.List;

public class DelWarpCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.MANAGE_WARPS.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        if (args.length < 1) return false;

        Location warp = Warps.getWarp(args[0]);
        if (warp == null) {
            player.sendMessage(Component.text("Warp \"")
                    .append(Component.text(args[0]).color(ERROR_ACCENT_COLOR))
                    .append(Component.text("\" does not exist!"))
                    .color(ERROR_COLOR));
            return true;
        }

        Warps.removeWarp(args[0]);
        player.sendMessage(Component.text("Warp \"")
                .append(Component.text(args[0]).color(INFO_ACCENT_COLOR))
                .append(Component.text("\" has been deleted."))
                .color(INFO_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (!(sender instanceof Player)) return out;

        if (args.length == 1) {
            out.addAll(Warps.getWarps());
        }

        return out;
    }
}
