package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.events.AfkManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class AfkCommand extends NautilusCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player;
        if (strings.length == 0) {
            if (!(sender instanceof Player p)) {
                return false;
            }
            player = p;
        } else {
            if (!sender.hasPermission(NautilusCommand.MODIFY_OTHER_PERM)) {
                sender.sendMessage(Component.text("Not enough permissions").style(Style.style(NautilusCommand.ERROR_COLOR)));
                return true;
            }

            player = Bukkit.getPlayerExact(strings[0]);
            if (player == null) {
                sender.sendMessage(Component.text("Player not found").style(Style.style(NautilusCommand.ERROR_COLOR)));
                return true;
            }
        }

        AfkManager.toggleAFK(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1 && commandSender.hasPermission(NautilusCommand.MODIFY_OTHER_PERM)) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map(Util::getName).toList());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
