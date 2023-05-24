package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class TpaCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (!player.hasPermission(TPA_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (strings.length < 1) return false;

        Player recipient = Util.getOnlinePlayer(strings[0]);
        if (recipient == null) {
            commandSender.sendMessage(Component.text("Player not found").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (recipient == commandSender) {
            commandSender.sendMessage(Component.text("You can't teleport to yourself!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        TpaManager.tpRequest(player, recipient, TpaManager.TpRequestType.TP_TO);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map(Util::getName).toList());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
