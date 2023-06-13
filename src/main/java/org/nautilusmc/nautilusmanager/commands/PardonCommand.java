package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class PardonCommand extends Command {
    public static final Component NOT_BANNED_ERROR = Component.text("That player is not banned!", ERROR_COLOR);

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(Permission.BAN.toString())) {
            sender.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        if (args.length < 1) return false;

        OfflinePlayer target = null;
        for (OfflinePlayer banned : Bukkit.getBannedPlayers()) {
            if (args[0].equals(banned.getName())) {
                target = banned;
                break;
            }
        }
        if (target == null) {
            sender.sendMessage(NOT_BANNED_ERROR);
            return true;
        }

        Bukkit.getBanList(BanList.Type.NAME).pardon(target.getUniqueId().toString());

        sender.sendMessage(Component.text("Successfully pardoned ")
                .append(Component.text(Util.getName(target)).color(INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(INFO_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (!sender.hasPermission(Permission.BAN.toString())) return out;

        if (args.length == 1) {
            out.addAll(Bukkit.getBannedPlayers().stream().map(OfflinePlayer::getName).toList());
        }

        return out;
    }
}
