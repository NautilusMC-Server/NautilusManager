package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.events.VanishManager;

public class VanishCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        if (!player.hasPermission(Permission.VANISH)) {
            player.sendMessage(ErrorMessage.NO_PERMISSION);
            return true;
        }

        if (VanishManager.isVanished(player)) {
            VanishManager.unvanish(player);
            player.sendMessage(Component.text("You are now visible!").color(Default.INFO_COLOR));
        } else {
            VanishManager.vanish(player);
            player.sendMessage(Component.text("You are now invisible!").color(Default.INFO_COLOR));
        }

        return true;
    }
}
