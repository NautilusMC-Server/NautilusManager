package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.VanishManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VanishCommand extends NautilusCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (!player.hasPermission(VANISH_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (VanishManager.isVanished(player)) {
            VanishManager.unvanish(player);
            player.sendMessage(Component.text("You are now visible!").color(NautilusCommand.MAIN_COLOR));
        } else {
            VanishManager.vanish(player);
            player.sendMessage(Component.text("You are now invisible!").color(NautilusCommand.MAIN_COLOR));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
