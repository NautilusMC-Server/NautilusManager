package org.nautilusmc.nautilusmanager.teleport.commands.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.Warps;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateWarpCommand extends NautilusCommand {

    private static final List<UUID> confirming = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        if (!player.hasPermission(Permission.CREATE_WARPS)) {
            player.sendMessage(ErrorMessage.NO_PERMISSION);
            return true;
        }

        if (strings.length < 1) return false;

        if (strings[0].length() > Warps.MAX_NAME_LENGTH) {
            player.sendMessage(Component.text("Warp name cannot be more than ")
                    .append(Component.text(Warps.MAX_NAME_LENGTH).color(Default.ERROR_ACCENT_COLOR))
                    .append(Component.text(" characters!"))
                    .color(Default.ERROR_COLOR));
            return true;
        }

        boolean overriding = Warps.getWarp(strings[0]) != null;
        if (overriding && !confirming.contains(player.getUniqueId())) {
            // TODO: this should probably use ConfirmationMessage instead
            player.sendMessage(Component.text("There is already a warp with that name. Run \"")
                    .append(Util.clickableCommand("/createwarp " + strings[0], true).color(Default.INFO_ACCENT_COLOR))
                    .append(Component.text("\" again to overwrite it."))
                    .color(Default.INFO_COLOR));
            confirming.add(player.getUniqueId());
            return true;
        }
        confirming.remove(player.getUniqueId());

        Warps.createWarp(strings[0], player.getLocation());
        player.sendMessage(Component.text("Created warp \"")
                .append(Component.text(strings[0]).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("\"."))
                .color(Default.INFO_COLOR));

        return true;
    }
}
