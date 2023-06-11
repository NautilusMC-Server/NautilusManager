package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class TpaCommand extends Command {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.TPA)) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        if (strings.length < 1) return false;

        Player recipient = Util.getOnlinePlayer(strings[0]);
        if (recipient == null) {
            commandSender.sendMessage(Command.INVALID_PLAYER_ERROR);
            return true;
        }

        if (recipient.equals(commandSender)) {
            commandSender.sendMessage(TpaManager.CANNOT_TP_TO_SELF_ERROR);
            return true;
        }

        if (TpaManager.isTrusted(recipient, player)) {
            recipient.sendMessage(Component.empty()
                    .append(Component.text(Util.getName(player)).color(Command.ACCENT_COLOR))
                    .append(Component.text(" is teleporting to you."))
                    .color(Command.MAIN_COLOR));
            TpaManager.performTp(recipient, player, TpaManager.TpRequestType.TP_TO);
        } else {
            TpaManager.tpRequest(player, recipient, TpaManager.RequestType.TO_RECIPIENT);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.addAll(getOnlineNames());
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
