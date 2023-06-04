package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.events.AfkManager;
import org.nautilusmc.nautilusmanager.events.GeneralEventManager;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MsgCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 2) return false;

        Player recipient = Util.getOnlinePlayer(strings[0]);
        if (recipient == null) {
            commandSender.sendMessage(ErrorMessage.INVALID_PLAYER);
            return true;
        }

        if (commandSender instanceof Player player) {
            if (MuteManager.isMuted(recipient, player)) {
                player.sendMessage(Component.text("This player has muted you!").color(NautilusCommand.ERROR_COLOR));
                return true;
            } else if (MuteManager.isMuted(player, recipient)) {
                player.sendMessage(Component.text("You have muted this player!").color(NautilusCommand.ERROR_COLOR));
                return true;
            }

            ReplyCommand.update(player.getUniqueId(), recipient.getUniqueId());
        }

        if (AfkManager.isAfk(recipient)) {
            GeneralEventManager.pingPlayer(recipient);
        }

        Component msg = MessageStyler.formatUserMessage(commandSender, Component.text(String.join(" ", Arrays.copyOfRange(strings, 1, strings.length))))
                .decorate(TextDecoration.ITALIC);

        Map.Entry<Component, Component> messages = styleWhisper(
                (commandSender instanceof Player p ?
                        p.displayName() :
                        commandSender.name()).decorate(TextDecoration.ITALIC),
                recipient.displayName().decorate(TextDecoration.ITALIC),
                msg);
        commandSender.sendMessage(messages.getKey());
        recipient.sendMessage(messages.getValue());

        return true;
    }

    public static Map.Entry<Component, Component> styleWhisper(Component sender, Component receiver, Component message) {
        return Map.entry(
                Component.empty()
                        .append(MessageStyler.getTimeStamp())
                        .append(Component.text(" You whispered to ").color(TextColor.color(150, 150, 150)).decorate(TextDecoration.ITALIC))
                        .append(receiver.decorate(TextDecoration.ITALIC))
                        .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                        .append(message),
                Component.empty()
                        .append(MessageStyler.getTimeStamp())
                        .append(Component.text(" "))
                        .append(sender.decorate(TextDecoration.ITALIC))
                        .append(Component.text(" whispered to you").color(TextColor.color(150, 150, 150)).decorate(TextDecoration.ITALIC))
                        .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                        .append(message));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.addAll(getOnlineNames());
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
