package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.ArrayList;
import java.util.List;

public class ChatMsgCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (args.length < 2) return false;

        if (args[0].equalsIgnoreCase("all")) {
            MessageStyler.sendMessageAsUser(player, Component.text(getMessageFromArgs(args, 1)));

        } else if (args[0].equalsIgnoreCase("player")) {
            if (args.length < 3) return false;

            player.performCommand("msg " + getMessageFromArgs(args, 0));

        } else if (args[0].equalsIgnoreCase("staff")) {
            if (!player.hasPermission(Permission.STAFF_CHAT.toString())) {
                player.sendMessage(NO_PERMISSION_ERROR);
                return true;
            }

            Component styledMessage = Component.empty()
                    .append(MessageStyler.getTimeStamp())
                    .append(Component.space()
                            .append(Component.text("["))
                            .append(Component.text("STAFF").color(TextColor.color(59, 214, 213)))
                            .append(Component.text("] "))
                            .color(TextColor.color(37, 129, 144))
                            .decorate(TextDecoration.BOLD))
                    .append(player.displayName())
                    .append(Component.text(" » ", TextColor.color(150, 150, 150)))
                    .append(MessageStyler.formatUserMessage(player, Component.text(getMessageFromArgs(args, 1)))
                            .decorate(TextDecoration.BOLD));

            Bukkit.getConsoleSender().sendMessage(styledMessage);
            for (Player recipient : Bukkit.getOnlinePlayers()) {
                if (recipient.hasPermission(Permission.STAFF_CHAT.toString())) {
                    recipient.sendMessage(styledMessage);
                }
            }

        } else {
            return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.add("all");
            out.add("player");
            if (sender.hasPermission(Permission.STAFF_CHAT.toString())) {
                out.add("staff");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("player")) {
            out.addAll(getOnlineNames());
        }

        return out;
    }
}
