package org.nautilusmc.nautilusmanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.AfkManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class AfkCommand extends NautilusCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player;
        if (strings.length == 0) {
            if (!(sender instanceof Player p)) {
                return false;
            }
            player = p;
        } else {
            if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS)) {
                sender.sendMessage(ErrorMessage.NO_PERMISSION);
                return true;
            }

            player = Bukkit.getPlayerExact(strings[0]);
            if (player == null) {
                sender.sendMessage(ErrorMessage.INVALID_PLAYER);
                return true;
            }
        }

        AfkManager.toggleAFK(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1 && commandSender.hasPermission(Permission.MODIFY_OTHER_PLAYERS)) {
            out.addAll(getOnlineNames());
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
