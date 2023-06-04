package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TpAcceptCommand extends NautilusCommand {
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

        TpaManager.acceptRequest(player, strings.length < 1 ? null : strings[0]);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (!(commandSender instanceof Player player)) return out;

        if (strings.length == 1) {
            out.addAll(TpaManager.incomingRequests(player).stream()
                    .map(Bukkit::getPlayer).filter(Objects::nonNull)
                    .map(p -> Nickname.getNickname(p) == null ? p.getName() : Nickname.getNickname(p))
                    .toList());
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
