package org.nautilusmc.nautilusmanager.crews.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.crews.Invite;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.ArrayList;
import java.util.List;

public class InviteCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }
        if (!player.hasPermission(Permission.JOIN_CREW.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase("accept")) {
            Invite.accept(player);
            return true;
        } else if (args[0].equalsIgnoreCase("decline")) {
            Invite.deny(player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        ArrayList<String> out = new ArrayList<>();

        if (!(sender instanceof Player player) || !player.hasPermission(Permission.INVITE_TO_CREW.toString())) return out;

        if (args.length == 1) {
            out.add("accept");
            out.add("decline");
        }

        return out;
    }
}
