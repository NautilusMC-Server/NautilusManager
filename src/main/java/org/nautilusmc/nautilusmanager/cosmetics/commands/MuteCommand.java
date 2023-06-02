package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.Arrays;
import java.util.List;

public class MuteCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        if (!player.hasPermission(MUTE_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        if (strings.length < 1) return false;

        OfflinePlayer toMute = Util.getOfflinePlayerIfCachedByNick(strings[0]);
        if (toMute == null) {
            player.sendMessage(Component.text("Player not found!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (toMute.getPlayer() == player) {
            player.sendMessage(Component.text("You can't mute yourself!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (MuteManager.getMuted(player).size() >= MuteManager.MAX_MUTED) {
            player.sendMessage(Component.text("You can't mute more than " + MuteManager.MAX_MUTED + " players!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        boolean isMuted = MuteManager.toggleMute(player, toMute);

        player.sendMessage(Component.empty()
                .append(Component.text(Util.getName(toMute)).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" is " + (isMuted ? "now" : "no longer")+ " muted."))
                .color(NautilusCommand.MAIN_COLOR));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String str, @NotNull String[] strings) {
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(Util::getName).filter(s->s.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
