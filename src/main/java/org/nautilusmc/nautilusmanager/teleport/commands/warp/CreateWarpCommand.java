package org.nautilusmc.nautilusmanager.teleport.commands.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Warps;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateWarpCommand extends Command {
    private static final List<UUID> PENDING_CONFIRMATIONS = new ArrayList<>();

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.MANAGE_WARPS.toString())) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        if (args.length < 1) return false;

        if (args[0].length() > Warps.MAX_NAME_LENGTH) {
            player.sendMessage(Component.text("Warp name cannot be more than ")
                    .append(Component.text(Warps.MAX_NAME_LENGTH).color(ERROR_ACCENT_COLOR))
                    .append(Component.text(" characters!"))
                    .color(ERROR_COLOR));
            return true;
        }

        boolean overriding = Warps.getWarp(args[0]) != null;
        if (overriding && !PENDING_CONFIRMATIONS.contains(player.getUniqueId())) {
            // TODO: this should probably use ConfirmationMessage instead
            player.sendMessage(Component.text("There is already a warp with that name. Run ")
                    .append(Util.clickableCommand("/createwarp " + args[0], true).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" again to overwrite it."))
                    .color(INFO_COLOR));
            PENDING_CONFIRMATIONS.add(player.getUniqueId());
            return true;
        }
        PENDING_CONFIRMATIONS.remove(player.getUniqueId());

        Warps.createWarp(args[0], player.getLocation());
        player.sendMessage(Component.text("Created warp \"")
                .append(Component.text(args[0]).color(INFO_ACCENT_COLOR))
                .append(Component.text("\"."))
                .color(INFO_COLOR));

        return true;
    }
}
