package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.MessageStyler;

import java.util.List;

public class LanguageCommand extends Command {
    private static final String[] KEYS = {
            "bg", "cs", "da", "de", "el", "en-us", "es", "et", "fi", "fr", "hu", "id", "it", "ja", "ko", "lt", "lv",
            "nb", "nl", "pl", "pt-br", "ro", "ru", "sk", "sl", "sv", "tr", "uk", "zh",
    };

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Your current language is ")
                    .append(Component.text(MessageStyler.getLanguage(player), INFO_ACCENT_COLOR))
                    .append(Component.text("."))
                    .color(INFO_COLOR));
            return true;
        }

        if (!List.of(KEYS).contains(args[0].toLowerCase())) {
            player.sendMessage(Component.text("That language is either invalid or unavailable.", ERROR_COLOR));
            return true;
        }

        MessageStyler.setLanguage(player, args[0].toLowerCase());
        player.sendMessage(Component.text("Your preferred language is now set to ")
                .append(Component.text(args[0].toLowerCase(), INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(INFO_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of(KEYS);
    }
}
