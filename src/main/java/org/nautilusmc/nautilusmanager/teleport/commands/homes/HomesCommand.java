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
    public static final ListDisplay<Map.Entry<String, Location>> HOME_LIST_DISPLAY = new ListDisplay<>(
            "Your Homes",
            6,
            null,
            (home) -> Component.empty()
                    .append(Component.text(home.getKey()).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" (%d, %d, %d)".formatted(home.getValue().getBlockX(), home.getValue().getBlockY(), home.getValue().getBlockZ())))
    );

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
        if (homes == null || homes.isEmpty()) {
            player.sendMessage(Component.text("  No homes set. Use ").color(INFO_COLOR)
                    .append(Util.clickableCommand("/sethome <name>", false).color(INFO_ACCENT_COLOR))
                    .append(Component.text(" to create your first home!")));
        } else {
            HOME_LIST_DISPLAY.setList(homes.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList());
            HOME_LIST_DISPLAY.sendPageTo(args.length >= 1 ? args[0] : null, player);
        }

        return true;
    }
}
