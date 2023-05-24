package org.nautilusmc.nautilusmanager.crews;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;

import java.util.ArrayList;

public class CrewsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        String out = "";
        ArrayList<Crew> crews = CrewHandler.getCrews();
        for (int i = 0; i < crews.size(); i++) {
            out += crews.get(i).getName();
            if (i < crews.size() - 1) {
                out += ", ";
            }
        }
        if (out.equals("")) {
            commandSender.sendMessage(Component.text("No crews on the server").color(NautilusCommand.DEFAULT_CHAT_TEXT_COLOR));
            return true;
        }
        commandSender.sendMessage(Component.text(out).color(NautilusCommand.DEFAULT_CHAT_TEXT_COLOR));
        return true;
    }
}
