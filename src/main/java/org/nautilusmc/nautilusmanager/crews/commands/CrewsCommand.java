package org.nautilusmc.nautilusmanager.crews.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.crews.Crew;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;
import org.nautilusmc.nautilusmanager.util.ListDisplay;

public class CrewsCommand extends Command {
    private static final ListDisplay<Crew> CREW_LIST_DISPLAY = new ListDisplay<>(
            "All Crews",
            10,
            null,
            (crew) -> Component.text("[")
                    .append(Component.text(crew.getPrefix(), INFO_ACCENT_COLOR))
                    .append(Component.text("]"))
                    .appendSpace()
                    .append(Component.text(crew.getName(), INFO_ACCENT_COLOR))
    );
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (CrewHandler.getCrews().isEmpty()) {
            sender.sendMessage(Component.text("There are currently no crews on the server.").color(INFO_COLOR));
            return true;
        }

        CREW_LIST_DISPLAY.setList(CrewHandler.getCrews());
        CREW_LIST_DISPLAY.sendPageTo(args.length >= 1 ? args[0] : null, sender);

        return true;
    }
}
