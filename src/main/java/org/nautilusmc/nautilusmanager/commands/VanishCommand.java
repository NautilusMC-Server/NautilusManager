package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.events.VanishManager;
import org.nautilusmc.nautilusmanager.util.Permission;

public class VanishCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.VANISH.toString())) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        if (VanishManager.isVanished(player)) {
            VanishManager.unvanish(player);
            player.sendMessage(Component.text("You are now ")
                    .append(Component.text("visible", INFO_ACCENT_COLOR))
                    .append(Component.text(" to other players."))
                    .color(INFO_COLOR));
        } else {
            VanishManager.vanish(player);
            player.sendMessage(Component.text("You are now ")
                    .append(Component.text("invisible", INFO_ACCENT_COLOR))
                    .append(Component.text(" to other players."))
                    .color(INFO_COLOR));
        }

        return true;
    }
}
