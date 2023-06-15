package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.ListDisplay;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.UUID;

public class TpTrustListCommand extends Command {
    private static final ListDisplay<UUID> TRUST_LIST_DISPLAY = new ListDisplay<UUID>("Trusted Players")
            .setFormatter(uuid -> Component.text(" - ")
                    .append(Component.text(Util.getName(Bukkit.getOfflinePlayer(uuid)), INFO_ACCENT_COLOR)))
            .setEmptyMessage(Component.text("You don't trust anyone! " + Emoji.FROWN));

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.TPA.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        TRUST_LIST_DISPLAY.setList(TpaManager.getTrusted(player));
        player.sendMessage(TRUST_LIST_DISPLAY.fetchPageContent(args.length >= 1 ? args[0] : null));

        return true;
    }
}
