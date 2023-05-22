package org.nautilusmc.nautilusmanager.teleport.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

public class HomeCommand extends NautilusCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        if (strings.length < 2) return false;

        TextColor color1 = TextColor.color(255, 188, 0);
        TextColor color2 = TextColor.color(255, 219, 140);

        switch (strings[0]) {
            case "set" -> {
                if (strings[1].length() > Homes.MAX_NAME_LEN) {
                    player.sendMessage(Component.text("Home names can be no longer than 16 characters").color(NautilusCommand.ERROR_COLOR));
                    return true;
                }

                Location loc = player.getLocation();

                player.sendMessage(Component.text("Set home ")
                        .append(Component.text(strings[1])
                                .color(color2))
                        .append(Component.text(" at "))
                        .append(Component.text(Integer.toString(loc.getBlockX()))
                                .color(color2))
                        .append(Component.text(", "))
                        .append(Component.text(Integer.toString(loc.getBlockY()))
                                .color(color2))
                        .append(Component.text(", "))
                        .append(Component.text(Integer.toString(loc.getBlockZ()))
                                .color(color2))
                        .color(color1));
                Homes.setHome(player, strings[1], loc);
            }
            case "del" -> {
                if (!Homes.getHomes(player).containsKey(strings[1])) {
                    player.sendMessage(Component.text("Home not found").color(NautilusCommand.ERROR_COLOR));
                    return true;
                }

                player.sendMessage(Component.text("Removed home ")
                        .append(Component.text(strings[1])
                                .color(color2))
                        .color(color1));
                Homes.setHome(player, strings[1], null);
            }
            case "tp" -> {

            }
            default -> {
                return false;
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (!(commandSender instanceof Player player)) return out;

        if (strings.length == 1) {
            out.add("set");
            out.add("del");
            out.add("tp");
        } else if (strings.length == 2 && (strings[1].equals("del") || strings[1].equals("tp"))) {
            if (Homes.getHomes(player) != null) out.addAll(Homes.getHomes(player).keySet());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
