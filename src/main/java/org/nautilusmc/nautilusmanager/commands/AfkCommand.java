package org.nautilusmc.nautilusmanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.AfkManager;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.ArrayList;
import java.util.List;

public class AfkCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player;
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                return false;
            }
            player = p;
        } else {
            if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
                sender.sendMessage(NO_PERMISSION_ERROR);
                return true;
            }

            player = Bukkit.getPlayerExact(args[0]);
            if (player == null) {
                sender.sendMessage(INVALID_PLAYER_ERROR);
                return true;
            }
        }

        AfkManager.toggleAFK(player);
        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
            out.addAll(getOnlineNames());
        }

        return out;
    }
}
