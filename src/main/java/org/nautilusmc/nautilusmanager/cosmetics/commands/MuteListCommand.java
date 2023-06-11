package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.util.ListDisplay;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.UUID;

public class MuteListCommand extends Command {
    private static final ListDisplay<UUID> MUTED_LIST_DISPLAY = new ListDisplay<>(
            "Muted Players",
            10,
            null,
            (uuid) -> Component.text(Util.getName(Bukkit.getOfflinePlayer(uuid)), INFO_ACCENT_COLOR)
    );

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

        MUTED_LIST_DISPLAY.setList(MuteManager.getMuted(player));
        MUTED_LIST_DISPLAY.sendPageTo(args.length >= 1 ? args[0] : null, player);

        return true;
    }
}
