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
    private static final ListDisplay<UUID> MUTED_LIST_DISPLAY = new ListDisplay<UUID>("Muted Players")
            .setFormatter(uuid -> Component.text(" - ")
                    .append(Component.text(Util.getName(Bukkit.getOfflinePlayer(uuid)), INFO_ACCENT_COLOR)))
            .setEmptyMessage(Component.text("You do not have any players muted."));

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
        player.sendMessage(MUTED_LIST_DISPLAY.fetchPageContent(args.length >= 1 ? args[0] : null));

        return true;
    }
}
