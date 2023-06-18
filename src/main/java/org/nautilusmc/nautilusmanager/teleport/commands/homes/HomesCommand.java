package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.util.ListDisplay;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.Map;

public class HomesCommand extends Command {
    public static final ListDisplay<Map.Entry<String, Location>> HOME_LIST_DISPLAY = new ListDisplay<Map.Entry<String, Location>>("Your Homes", 6)
            .setFormatter(home -> Component.empty()
                    .append(Component.text(home.getKey()).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" (%d, %d, %d)".formatted(home.getValue().getBlockX(), home.getValue().getBlockY(), home.getValue().getBlockZ()))))
            .setEmptyMessage(Component.text("No homes set. Use ")
                    .append(Util.clickableCommand("/sethome <name>", false).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" to create your first home!")));

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

        Map<String, Location> homes = Homes.getHomes(player);
        HOME_LIST_DISPLAY.setList(homes == null ? null : homes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList());
        player.sendMessage(HOME_LIST_DISPLAY.fetchPageContent(args.length >= 1 ? args[0] : null));

        return true;
    }
}
