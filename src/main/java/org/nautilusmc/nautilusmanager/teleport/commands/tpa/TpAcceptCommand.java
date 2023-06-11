package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TpAcceptCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.TPA.toString())) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        TpaManager.acceptRequest(player, args.length < 1 ? null : args[0]);

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (!(sender instanceof Player player)) return out;

        if (args.length == 1) {
            out.addAll(TpaManager.incomingRequests(player).stream()
                    .map(Bukkit::getPlayer).filter(Objects::nonNull)
                    .map(Util::getName)
                    .toList());
        }

        return out;
    }
}
