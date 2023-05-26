package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.crews.Crew;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;

import java.util.ArrayList;

public class CrewsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<Crew> crews = CrewHandler.getCrews();
        if (crews.isEmpty()) {
            commandSender.sendMessage(Component.text("No crews on the server!").color(NautilusCommand.MAIN_COLOR));
            return true;
        }
        Component out = Component.text("Crews: ").color(NautilusCommand.MAIN_COLOR);
        for (int i = 0; i < crews.size(); i++) {
            out = out.append(Component.text(crews.get(i).getName()).color(NautilusCommand.ACCENT_COLOR));
            if (i < crews.size() - 1) {
                out = out.append(Component.text(", ").color(NautilusCommand.ACCENT_COLOR));
            }
        }
        commandSender.sendMessage(out);
        return true;
    }
}
