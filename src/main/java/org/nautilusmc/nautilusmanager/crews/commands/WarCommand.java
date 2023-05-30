package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.crews.Invite;
import org.nautilusmc.nautilusmanager.crews.WarDeclaration;

import java.util.ArrayList;
import java.util.List;

public class WarCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text("Only players can use this command").color(ERROR_COLOR));
            return true;
        }
        Player player = (Player) commandSender;
        if (!(player.hasPermission(NautilusCommand.DECLARE_WAR_PERM))) {
            error(player, CrewCommand.CAPTAIN_PERM_MESSAGE);
            return  true;
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tabCompletions = new ArrayList<>();
        tabCompletions.add("accept");
        tabCompletions.add("decline");
        return tabCompletions;
    }
}
