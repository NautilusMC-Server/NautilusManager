package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetHomeCommand extends NautilusCommand {

    private static final List<UUID> confirming = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (!player.hasPermission(HOMES_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (strings.length < 1) return false;

        if (strings[0].length() > Homes.MAX_NAME_LEN) {
            player.sendMessage(Component.text("Home names can be no longer than 16 characters").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        boolean overriding = Homes.getHome(player, strings[0]) != null;
        if (overriding && !confirming.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("You already have a home with that name, run ")
                    .append(Util.clickableCommand("/sethome "+strings[0], true).color(NautilusCommand.ACCENT_COLOR))
                    .append(Component.text(" again to overwrite it"))
                    .color(NautilusCommand.MAIN_COLOR));
            confirming.add(player.getUniqueId());
            return true;
        }
        confirming.remove(player.getUniqueId());

        if (!overriding && (Homes.getHomes(player) != null && Homes.getHomes(player).size() >= Homes.getMaxHomes(player))) {
            player.sendMessage(Component.text("You already have the maximum amount of homes, ")
                    .append(Util.clickableCommand("/delhome ", false).color(NautilusCommand.ACCENT_COLOR))
                    .append(Component.text("to remove one or "))
                    .append(Util.clickableCommand("/buyhome ", false).color(NautilusCommand.ACCENT_COLOR))
                    .append(Component.text("to buy a new one."))
                    .color(NautilusCommand.MAIN_COLOR));
            return true;
        }

        Location loc = player.getLocation();

        player.sendMessage(Component.text("Set home ")
                .append(Component.text(strings[0])
                        .color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" at "))
                .append(Component.text(Integer.toString(loc.getBlockX()))
                        .color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(", "))
                .append(Component.text(Integer.toString(loc.getBlockY()))
                        .color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(", "))
                .append(Component.text(Integer.toString(loc.getBlockZ()))
                        .color(NautilusCommand.ACCENT_COLOR))
                .color(NautilusCommand.MAIN_COLOR));
        Homes.setHome(player, strings[0], loc);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
