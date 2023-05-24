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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomesCommand extends NautilusCommand {
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

        player.sendMessage(Component.text("Homes: ").color(NautilusCommand.MAIN_COLOR));
        if (Homes.getHomes(player) == null || Homes.getHomes(player).isEmpty()) {
            player.sendMessage(Component.text("  Nothing to see here yet, run ").color(NautilusCommand.MAIN_COLOR)
                    .append(Component.text("/sethome <name>").color(NautilusCommand.ACCENT_COLOR))
                    .append(Component.text("!")));
        } else {
            for (Map.Entry<String, Location> home : Homes.getHomes(player).entrySet()) {
                Location loc = home.getValue();
                player.sendMessage(Component.text(" - ").color(NautilusCommand.MAIN_COLOR).append(Component.text(home.getKey()).color(NautilusCommand.ACCENT_COLOR))
                        .append(Component.text(" (%d,%d,%d)".formatted(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
