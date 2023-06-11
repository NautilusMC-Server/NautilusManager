package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MuteCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }
        if (!player.hasPermission(Permission.PERSONAL_MUTE.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }
        if (args.length < 1) return false;

        OfflinePlayer toMute = Util.getOfflinePlayerIfCachedByNick(args[0]);
        if (toMute == null) {
            player.sendMessage(INVALID_PLAYER_ERROR);
            return true;
        }

        if (toMute.getPlayer() == player) {
            player.sendMessage(Component.text("You can't mute yourself!").color(ERROR_COLOR));
            return true;
        }

        if (MuteManager.getMuted(player).size() >= MuteManager.MAX_MUTED) {
            player.sendMessage(Component.text("You can't mute more than " + MuteManager.MAX_MUTED + " players!").color(ERROR_COLOR));
            return true;
        }

        boolean isMuted = MuteManager.toggleMute(player, toMute);

        player.sendMessage(Component.empty()
                .append(Component.text(Util.getName(toMute)).color(INFO_ACCENT_COLOR))
                .append(Component.text(" is " + (isMuted ? "now" : "no longer") + " muted."))
                .color(INFO_COLOR));
        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.addAll(getOfflineNames());
        }

        return out;
    }
}
