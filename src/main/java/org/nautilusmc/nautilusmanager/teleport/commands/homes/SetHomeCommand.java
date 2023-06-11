package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SetHomeCommand extends Command {
    private static final List<UUID> PENDING_CONFIRMATIONS = new ArrayList<>();

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.USE_HOMES.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        if (args.length < 1) return false;

        if (args[0].length() > Homes.MAX_NAME_LENGTH) {
            player.sendMessage(Component.text("Home name cannot be more than ")
                    .append(Component.text(Homes.MAX_NAME_LENGTH).color(ERROR_ACCENT_COLOR))
                    .append(Component.text(" characters!"))
                    .color(ERROR_COLOR));
            return true;
        }

        boolean overriding = Homes.getHome(player, args[0]) != null;
        if (overriding && !PENDING_CONFIRMATIONS.contains(player.getUniqueId())) {
            // TODO: this should probably use ConfirmationMessage instead
            player.sendMessage(Component.text("You already have a home with that name. Run \"")
                    .append(Util.clickableCommand("/sethome " + args[0], true).color(INFO_ACCENT_COLOR))
                    .append(Component.text("\" again to overwrite it."))
                    .color(INFO_COLOR));
            PENDING_CONFIRMATIONS.add(player.getUniqueId());
            return true;
        }
        PENDING_CONFIRMATIONS.remove(player.getUniqueId());

        Map<String, Location> homes = Homes.getHomes(player);
        if (!overriding && (homes != null && homes.size() >= Homes.getMaxHomes(player))) {
            player.sendMessage(Component.text("You already have the maximum number of homes. Consider using ")
                    .append(Util.clickableCommand("/delhome", false).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" to remove one or "))
                    .append(Util.clickableCommand("/buyhome", false).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" to buy a new one."))
                    .color(INFO_COLOR));
            return true;
        }

        Location location = player.getLocation();
        Homes.setHome(player, args[0], location);
        player.sendMessage(Component.text("Set home \"")
                .append(Component.text(args[0]).color(INFO_ACCENT_COLOR))
                .append(Component.text("\" at "))
                .append(Component.text(location.getBlockX()).color(INFO_ACCENT_COLOR))
                .append(Component.text(", "))
                .append(Component.text(location.getBlockY()).color(INFO_ACCENT_COLOR))
                .append(Component.text(", "))
                .append(Component.text(location.getBlockZ()).color(INFO_ACCENT_COLOR))
                .color(INFO_COLOR));

        return true;
    }
}
