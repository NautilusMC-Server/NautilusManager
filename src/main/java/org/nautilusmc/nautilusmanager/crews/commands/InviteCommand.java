package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.crews.Invite;

import java.util.ArrayList;
import java.util.List;

public class InviteCommand extends NautilusCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text("Only players can use this command").color(ERROR_COLOR));
            return true;
        }
        Player player = (Player) commandSender;
        if (!(player.hasPermission(NautilusCommand.JOIN_CREW_PERM))) {
            error(player, DEFAULT_PERM_MESSAGE);
            return  true;
        }
        if (strings[0].equals("accept")) {
            Invite.accept(player);
            return true;
        } else if (strings[0].equals("decline")) {
            Invite.deny(player);
            return true;
        } else {
            return false;
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
