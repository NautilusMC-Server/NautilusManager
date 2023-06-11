package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.events.AFKManager;
import org.nautilusmc.nautilusmanager.events.GeneralEventManager;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MsgCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) return false;

        Player recipient = Util.getOnlinePlayer(args[0]);
        if (recipient == null) {
            sender.sendMessage(Command.INVALID_PLAYER_ERROR);
            return true;
        }

        if (sender instanceof Player player) {
            if (MuteManager.isMuted(recipient, player)) {
                player.sendMessage(Component.text("This player has muted you!").color(Command.ERROR_COLOR));
                return true;
            } else if (MuteManager.isMuted(player, recipient)) {
                player.sendMessage(Component.text("You have muted this player!").color(Command.ERROR_COLOR));
                return true;
            }

            ReplyCommand.update(player.getUniqueId(), recipient.getUniqueId());
        }

        if (AFKManager.isAFK(recipient)) {
            GeneralEventManager.pingPlayer(recipient);
        }

        Component msg = MessageStyler.formatUserMessage(sender, Component.text(getMessageFromArgs(args, 1)))
                .decorate(TextDecoration.ITALIC);

        Map.Entry<Component, Component> messages = styleWhisper(
                (sender instanceof Player p ? p.displayName() : sender.name()).decorate(TextDecoration.ITALIC),
                recipient.displayName().decorate(TextDecoration.ITALIC),
                msg);
        sender.sendMessage(messages.getKey());
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
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.addAll(getOnlineNames());
        }

        return out;
    }
}
