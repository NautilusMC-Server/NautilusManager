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
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.Arrays;
import java.util.List;

public class TpTrustCommand extends Command {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.").color(Command.ERROR_COLOR));
            return true;
        }
        if (!player.hasPermission(TPA_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(Command.ERROR_COLOR));
            return true;
        }
        if (strings.length < 1) return false;

        OfflinePlayer trusted = Util.getOfflinePlayerIfCachedByNick(strings[0]);
        if (trusted == null) {
            player.sendMessage(Component.text("Player not found!").color(Command.ERROR_COLOR));
            return true;
        }

        if (trusted.getPlayer() == player) {
            player.sendMessage(Component.text("You don't need to trust yourself!").color(Command.ERROR_COLOR));
            return true;
        }

        if (TpaManager.getTrusted(player).size() >= TpaManager.MAX_TRUSTED) {
            player.sendMessage(Component.text("You can't trust more than " + TpaManager.MAX_TRUSTED + " players!").color(Command.ERROR_COLOR));
            return true;
        }

        boolean isTrusted = TpaManager.toggleTrust(player, trusted);

        player.sendMessage(Component.empty()
                .append(Component.text(Util.getName(trusted)).color(Command.ACCENT_COLOR))
                .append(Component.text(" is " + (isTrusted ? "now" : "no longer")+ " trusted."))
                .color(Command.MAIN_COLOR));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String str, @NotNull String[] strings) {
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(Util::getName).filter(s->s.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
