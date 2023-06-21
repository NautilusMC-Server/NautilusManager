package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.ListDisplay;

import java.util.ArrayList;
import java.util.List;

public class EmojiCommand extends Command {
    private static final ListDisplay<Emoji> EMOJI_LIST_DISPLAY = new ListDisplay<Emoji>("All Emojis", 12)
            .setFormatter(emoji -> Component.empty()
                    .append(Component.text(emoji.toString(), INFO_ACCENT_COLOR))
                    .append(Component.text(" | :" + emoji.name().toLowerCase() + ":")))
            .setList(List.of(Emoji.values()));

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        boolean listAll = args.length < 1 || args[0].equalsIgnoreCase("list");
        if (!listAll && !args[0].equalsIgnoreCase("find")) return false;

        if (listAll) {
            sender.sendMessage(EMOJI_LIST_DISPLAY.fetchPageContent(args.length >= 2 ? args[1] : null));
        } else {
            sender.sendMessage(Component.text("Not implemented yet.", Command.INFO_COLOR));
        }

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.add("list");
            out.add("find");
        }

        return out;
    }
}
