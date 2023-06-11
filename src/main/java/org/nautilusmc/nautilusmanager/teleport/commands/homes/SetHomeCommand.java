package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SetHomeCommand extends Command {

    private static final List<UUID> confirming = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.USE_HOMES)) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        if (strings.length < 1) return false;

        if (strings[0].length() > Homes.MAX_NAME_LENGTH) {
            player.sendMessage(Component.text("Home name cannot be more than ")
                    .append(Component.text(Homes.MAX_NAME_LENGTH).color(Default.ERROR_ACCENT_COLOR))
                    .append(Component.text(" characters!"))
                    .color(Default.ERROR_COLOR));
            return true;
        }

        boolean overriding = Homes.getHome(player, strings[0]) != null;
        if (overriding && !confirming.contains(player.getUniqueId())) {
            // TODO: this should probably use ConfirmationMessage instead
            player.sendMessage(Component.text("You already have a home with that name. Run \"")
                    .append(Util.clickableCommand("/sethome " + strings[0], true).color(Default.INFO_ACCENT_COLOR))
                    .append(Component.text("\" again to overwrite it."))
                    .color(Default.INFO_COLOR));
            confirming.add(player.getUniqueId());
            return true;
        }
        confirming.remove(player.getUniqueId());

        Map<String, Location> homes = Homes.getHomes(player);
        if (!overriding && (homes != null && homes.size() >= Homes.getMaxHomes(player))) {
            player.sendMessage(Component.text("You already have the maximum number of homes. Consider using ")
                    .append(Util.clickableCommand("/delhome", false).color(Default.INFO_ACCENT_COLOR))
                    .append(Component.text(" to remove one or "))
                    .append(Util.clickableCommand("/buyhome", false).color(Default.INFO_ACCENT_COLOR))
                    .append(Component.text(" to buy a new one."))
                    .color(Default.INFO_COLOR));
            return true;
        }

        Location location = player.getLocation();
        Homes.setHome(player, strings[0], location);
        player.sendMessage(Component.text("Set home \"")
                .append(Component.text(strings[0]).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text("\" at "))
                .append(Component.text(location.getBlockX()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(", "))
                .append(Component.text(location.getBlockY()).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(", "))
                .append(Component.text(location.getBlockZ()).color(Default.INFO_ACCENT_COLOR))
                .color(Default.INFO_COLOR));
        return true;
    }
}
