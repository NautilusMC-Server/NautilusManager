package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.crews.WarDeclaration;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.ArrayList;
import java.util.List;

public class WarCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command").color(ERROR_COLOR));
            return true;
        }
        if (!player.hasPermission(Permission.DECLARE_WAR.toString())) {
            player.sendMessage(Command.NOT_CAPTAIN_ERROR);
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
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
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        ArrayList<String> out = new ArrayList<>();

        if (!(sender instanceof Player player) || !player.hasPermission(Permission.DECLARE_WAR.toString())) return out;

        if (args.length == 1) {
            out.add("accept");
            out.add("decline");
        }

        return out;
    }
}
