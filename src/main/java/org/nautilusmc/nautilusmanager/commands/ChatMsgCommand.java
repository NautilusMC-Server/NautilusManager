package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatMsgCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("You must be a player to use this command").color(Default.ERROR_COLOR));
            return true;
        }
        if (strings.length < 2) return false;

        if (strings[0].equalsIgnoreCase("all")) {
            MessageStyler.sendMessageAsUser(player, Component.text(String.join(" ", Arrays.copyOfRange(strings, 1, strings.length))));
            return true;
        } else if (strings[0].equalsIgnoreCase("player")) {
            if (strings.length < 3) return false;

            player.performCommand("msg " + strings[1] + " " + String.join(" ", Arrays.copyOfRange(strings, 2, strings.length)));

            return true;
        } else if (strings[0].equalsIgnoreCase("staff")) {
            if (!player.hasPermission(Permission.STAFF_CHAT)) {
                player.sendMessage(Component.text("Not enough permissions").color(Default.ERROR_COLOR));
                return true;
            }

            String msg = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));

            Component message = Component.empty()
                    .append(Component.empty()
                            .append(Component.text("["))
                            .append(Component.text("STAFF").color(TextColor.color(59, 214, 213)))
                            .append(Component.text("] "))
                            .color(TextColor.color(37, 129, 144))
                            .decorate(TextDecoration.BOLD))
                    .append(player.displayName())
                    .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                    .append(MessageStyler.formatUserMessage(player, Component.text(msg)).decorate(TextDecoration.BOLD));
            Bukkit.getConsoleSender().sendMessage(message);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission(Permission.STAFF_CHAT)) {
                    p.sendMessage(message);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.add("all");
            out.add("player");
            if (commandSender.hasPermission(Permission.STAFF_CHAT)) {
                out.add("staff");
            }
        }

        if (strings.length == 2 && strings[0].equalsIgnoreCase("player")) {
            out.addAll(getOnlineNames());
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
