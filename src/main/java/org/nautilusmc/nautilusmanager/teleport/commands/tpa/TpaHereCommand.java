package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

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

public class TpaHereCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        if (!player.hasPermission(Permission.TPA)) {
            player.sendMessage(ErrorMessage.NO_PERMISSION);
            return true;
        }

        if (strings.length < 1) return false;

        Player recipient = Util.getOnlinePlayer(strings[0]);
        if (recipient == null) {
            commandSender.sendMessage(ErrorMessage.INVALID_PLAYER);
            return true;
        }

        if (recipient.equals(commandSender)) {
            commandSender.sendMessage(ErrorMessage.CANNOT_TP_TO_SELF);
            return true;
        }

        TpaManager.tpRequest(player, recipient, TpaManager.RequestType.TO_REQUESTER);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.addAll(getOnlineNames());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
