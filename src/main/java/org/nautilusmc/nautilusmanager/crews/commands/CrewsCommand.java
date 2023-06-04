package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;

public class CrewsCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (CrewHandler.CREWS.isEmpty()) {
            commandSender.sendMessage(Component.text("There are currently no crews on the server.").color(Default.INFO_COLOR));
            return true;
        }
        Component out = Component.text("Crews: ").color(Default.INFO_COLOR);
        for (int i = 0; i < CrewHandler.CREWS.size(); i++) {
            out = out.append(Component.text(CrewHandler.CREWS.get(i).getName()).color(Default.INFO_ACCENT_COLOR));
            if (i < CrewHandler.CREWS.size() - 1) {
                out = out.append(Component.text(", ").color(Default.INFO_COLOR));
            }
        }
        commandSender.sendMessage(out);
        return true;
    }
}
