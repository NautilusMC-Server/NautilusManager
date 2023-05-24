package org.nautilusmc.nautilusmanager.teleport.commands.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.teleport.Warps;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.HomeCommand;

import java.util.ArrayList;
import java.util.List;

public class DeleteWarpCommand extends NautilusCommand {
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

        Warps.removeWarp(strings[0]);
        player.sendMessage(Component.text("Removed warp ")
                .append(Component.text(strings[0]).color(HomeCommand.COLOR_2))
                .color(HomeCommand.COLOR_1));

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
