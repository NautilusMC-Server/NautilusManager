package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.Arrays;
import java.util.List;

public class TpTrustCommand extends Command {
    public static final Component CANNOT_TRUST_SELF_ERROR = Component.text("You don't need to trust yourself!").color(ERROR_COLOR);

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
        if (args.length < 1) return false;

        OfflinePlayer target = Util.getOfflinePlayerIfCachedByNick(args[0]);
        if (target == null) {
            player.sendMessage(INVALID_PLAYER_ERROR);
            return true;
        }

        if (target.getPlayer() == player) {
            player.sendMessage(CANNOT_TRUST_SELF_ERROR);
            return true;
        }

        if (TpaManager.getTrusted(player).size() >= TpaManager.MAX_TRUSTED) {
            player.sendMessage(Component.text("You can't trust more than " + TpaManager.MAX_TRUSTED + " players!").color(ERROR_COLOR));
            return true;
        }

        boolean trusted = TpaManager.toggleTrust(player, target);

        player.sendMessage(Component.empty()
                .append(Component.text(Util.getName(target)).color(INFO_ACCENT_COLOR))
                .append(Component.text(" is " + (trusted ? "now" : "no longer") + " trusted."))
                .color(INFO_COLOR));
        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        return getOfflineNames();
    }
}
