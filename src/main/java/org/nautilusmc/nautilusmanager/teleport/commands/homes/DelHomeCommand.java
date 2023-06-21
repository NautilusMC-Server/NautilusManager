package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelHomeCommand extends Command {
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

        if (Homes.getHome(player, args[0]) == null) {
            player.sendMessage(Component.text("Home \"")
                    .append(Component.text(args[0]).color(ERROR_ACCENT_COLOR))
                    .append(Component.text("\" does not exist!"))
                    .color(ERROR_COLOR));
            return true;
        }

        Homes.setHome(player, args[0], null);
        player.sendMessage(Component.text("Home \"")
                .append(Component.text(args[0]).color(INFO_ACCENT_COLOR))
                .append(Component.text("\" has been deleted."))
                .color(INFO_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (!(sender instanceof Player player)) return out;

        if (args.length == 1) {
            Map<String, Location> homes = Homes.getHomes(player);
            if (homes != null) {
                out.addAll(homes.keySet());
            }
        }

        return out;
    }
}
