package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.Homes;

import java.util.ArrayList;
import java.util.List;

public class DelHomeCommand extends NautilusCommand {
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

        if (strings.length < 1) return false;

        if (Homes.getHome(player, strings[0]) == null) {
            player.sendMessage(Component.text("Home not found").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        player.sendMessage(Component.text("Removed home ")
                .append(Component.text(strings[0])
                        .color(NautilusCommand.ACCENT_COLOR))
                .color(NautilusCommand.MAIN_COLOR));
        Homes.setHome(player, strings[0], null);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (!(commandSender instanceof Player player)) return out;

        if (strings.length == 1) {
            if (Homes.getHomes(player) != null) out.addAll(Homes.getHomes(player).keySet());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
