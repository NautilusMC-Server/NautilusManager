package org.nautilusmc.nautilusmanager.teleport.commands.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.Warps;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateWarpCommand extends NautilusCommand {

    private static final List<UUID> confirming = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (!player.hasPermission(CREATE_WARPS_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (strings.length < 1) return false;

        if (strings[0].length() > Warps.MAX_NAME_LEN) {
            player.sendMessage(Component.text("Name too long").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (Warps.getWarp(strings[0]) != null && !confirming.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("There is already a warp with that name, run ")
                    .append(Util.clickableCommand("/createwarp "+strings[0], true).color(NautilusCommand.ACCENT_COLOR))
                    .append(Component.text(" again to overwrite it"))
                    .color(NautilusCommand.MAIN_COLOR));
            confirming.add(player.getUniqueId());
            return true;
        }
        confirming.remove(player.getUniqueId());


        Warps.newWarp(strings[0], player.getLocation());
        player.sendMessage(Component.text("Created warp ")
                .append(Component.text(strings[0]).color(NautilusCommand.ACCENT_COLOR))
                .color(NautilusCommand.MAIN_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
