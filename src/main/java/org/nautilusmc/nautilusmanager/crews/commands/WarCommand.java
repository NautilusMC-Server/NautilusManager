package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.crews.WarDeclaration;

import java.util.ArrayList;
import java.util.List;

public class WarCommand extends Command {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command").color(Default.ERROR_COLOR));
            return true;
        }
        if (!player.hasPermission(Permission.DECLARE_WAR)) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        if (strings.length == 0) {
            return false;
        }
        switch (strings[0]) {
            case "accept" -> {
                WarDeclaration.accept(player);
                return true;
            }
            case "decline" -> {
                WarDeclaration.deny(player);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tabCompletions = new ArrayList<>();

        if (!(commandSender instanceof Player player) || !player.hasPermission(Permission.DECLARE_WAR)) return tabCompletions;

        if (strings.length == 1) {
            if (strings[0].toLowerCase().startsWith("accept")) tabCompletions.add("accept");
            if (strings[0].toLowerCase().startsWith("decline")) tabCompletions.add("decline");
        }

        return tabCompletions;
    }
}
