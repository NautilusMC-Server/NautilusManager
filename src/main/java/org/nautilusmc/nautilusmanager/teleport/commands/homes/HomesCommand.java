package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.Map;

public class HomesCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        if (!player.hasPermission(Permission.USE_HOMES)) {
            player.sendMessage(ErrorMessage.NO_PERMISSION);
            return true;
        }

        player.sendMessage(Component.text("Homes: ").color(Default.INFO_COLOR));
        Map<String, Location> homes = Homes.getHomes(player);
        if (homes == null || homes.isEmpty()) {
            player.sendMessage(Component.text("  No homes set. Use ").color(Default.INFO_COLOR)
                    .append(Util.clickableCommand("/sethome <name>", false).color(Default.INFO_ACCENT_COLOR))
                    .append(Component.text(" to create your first home!")));
        } else {
            for (Map.Entry<String, Location> home : homes.entrySet()) {
                Location location = home.getValue();
                player.sendMessage(Component.text(" - ").color(Default.INFO_COLOR).append(Component.text(home.getKey()).color(Default.INFO_ACCENT_COLOR))
                        .append(Component.text(" (%d, %d, %d)".formatted(location.getBlockX(), location.getBlockY(), location.getBlockZ()))));
            }
        }

        return true;
    }
}
