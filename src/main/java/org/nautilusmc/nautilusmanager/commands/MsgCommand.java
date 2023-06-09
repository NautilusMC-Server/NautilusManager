package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.events.AFKManager;
import org.nautilusmc.nautilusmanager.events.GeneralEventManager;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MsgCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) return false;

        Player recipient = Util.getOnlinePlayer(args[0]);
        if (recipient == null) {
            sender.sendMessage(INVALID_PLAYER_ERROR);
            return true;
        }

        if (sender instanceof Player player) {
            if (MuteManager.isMuted(recipient, player)) {
                player.sendMessage(Component.text("This player has muted you!", ERROR_COLOR));
                return true;
            } else if (MuteManager.isMuted(player, recipient)) {
                player.sendMessage(Component.text("You have muted this player!", ERROR_COLOR));
                return true;
            }

            ReplyCommand.update(player.getUniqueId(), recipient.getUniqueId());
        }

        Component message = MessageStyler.formatUserMessage(sender, Component.text(getMessageFromArgs(args, 1)));

        sender.sendMessage(MessageStyler.styleOutgoingWhisper(recipient.displayName(), message));
        recipient.sendMessage(MessageStyler.styleIncomingWhisper(sender instanceof Player player ? player.displayName() : sender.name(), message));

        if (AFKManager.isAFK(recipient)) {
            GeneralEventManager.pingPlayer(recipient);
        }

        return true;
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
