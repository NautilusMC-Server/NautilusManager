package org.nautilusmc.nautilusmanager.teleport.commands.tpa;

import net.kyori.adventure.text.Component;
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

public class TpaCommand extends Command {
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

        if (args.length < 1) return false;

        Player recipient = Util.getOnlinePlayer(args[0]);
        if (recipient == null) {
            sender.sendMessage(Command.INVALID_PLAYER_ERROR);
            return true;
        }

        if (recipient.equals(sender)) {
            sender.sendMessage(TpaManager.CANNOT_TP_TO_SELF_ERROR);
            return true;
        }

        if (TpaManager.isTrusted(recipient, player)) {
            recipient.sendMessage(Component.empty()
                    .append(Component.text(Util.getName(player)).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" is teleporting to you."))
                    .color(INFO_COLOR));
            TpaManager.performTeleport(recipient, player, TpaManager.TeleportRequest.REQUESTER_TO_RECIPIENT);
        } else {
            TpaManager.tpRequest(player, recipient, TpaManager.TeleportRequest.REQUESTER_TO_RECIPIENT);
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
