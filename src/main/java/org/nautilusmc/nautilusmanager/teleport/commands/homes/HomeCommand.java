package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;
import org.nautilusmc.nautilusmanager.teleport.Homes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand extends NautilusCommand {
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

        if (strings.length < 1) return false;

        if (Homes.getHome(player, strings[0]) == null) {
            player.sendMessage(Component.text("Could not find a home with the name \"")
                    .append(Component.text(strings[0]).color(Default.ERROR_ACCENT_COLOR))
                    .append(Component.text("\"!"))
                    .color(Default.ERROR_COLOR));
            return true;
        }

        TeleportHandler.teleportAfterDelay(player, Homes.getHome(player, strings[0]));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (!(commandSender instanceof Player player)) return out;

        if (strings.length == 1) {
            Map<String, Location> homes = Homes.getHomes(player);
            if (homes != null) {
                out.addAll(homes.keySet());
            }
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
