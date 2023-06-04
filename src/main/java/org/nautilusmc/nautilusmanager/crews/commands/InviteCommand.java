package org.nautilusmc.nautilusmanager.crews.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.crews.Invite;

import java.util.ArrayList;
import java.util.List;

public class InviteCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }
        if (!player.hasPermission(Permission.INVITE_TO_CREW)) {
            player.sendMessage(ErrorMessage.NO_PERMISSION);
            return true;
        }
        if (strings.length == 0) {
            return false;
        }
        if (strings[0].equalsIgnoreCase("accept")) {
            Invite.accept(player);
            return true;
        } else if (strings[0].equalsIgnoreCase("decline")) {
            Invite.deny(player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tabCompletions = new ArrayList<>();

        if (!(commandSender instanceof Player player) || !player.hasPermission(Permission.INVITE_TO_CREW)) return tabCompletions;

        if (strings.length == 1) {
            if (strings[0].toLowerCase().startsWith("accept")) tabCompletions.add("accept");
            if (strings[0].toLowerCase().startsWith("decline")) tabCompletions.add("decline");
        }

        return tabCompletions;
    }
}
