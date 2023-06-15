package org.nautilusmc.nautilusmanager.cosmetics.commands;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.ListDisplay;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FormattingCommand extends Command {
    private static final String EXAMPLE_STRING = "NautilusMC";

    private static final ListDisplay<ChatFormatting> CODES_LIST_DISPLAY = new ListDisplay<ChatFormatting>("Formatting Codes", 16)
            .setFormatter(formatting -> Component.text(" - ")
                    .append(Component.text("`" + formatting.getChar(), INFO_ACCENT_COLOR))
                    .append(Component.text(" " + Emoji.RIGHT + " "))
                    .append(Util.nmsFormat(Component.text(EXAMPLE_STRING), formatting)))
            .setList(List.of(ChatFormatting.values()));
    private static final ListDisplay<ChatFormatting> NAMES_LIST_DISPLAY = new ListDisplay<ChatFormatting>("Formatting Names", 16)
            .setFormatter(formatting -> Component.text(" - ")
                    .append(Component.text("``" + formatting.getName().toLowerCase(), INFO_ACCENT_COLOR))
                    .append(Component.text(" " + Emoji.RIGHT + " "))
                    .append(Util.nmsFormat(Component.text(EXAMPLE_STRING), formatting)))
            .setList(List.of(ChatFormatting.values()));

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(Permission.USE_CHAT_FORMATTING.toString())) {
            sender.sendMessage(Command.NOT_SPONSOR_ERROR);
            return true;
        }

        if (args.length >= 1) {
            boolean listNames = args[0].equalsIgnoreCase("names");
            if (!listNames && !args[0].equalsIgnoreCase("codes")) return false;

            sender.sendMessage((listNames ? NAMES_LIST_DISPLAY : CODES_LIST_DISPLAY).fetchPageContent(args.length >= 2 ? args[1] : null));

            return true;
        }

        sender.sendMessage(Component.empty()
                .append(Component.text("Codes: ").color(INFO_COLOR).decorate(TextDecoration.BOLD))
                .append(Component.text("`x").color(INFO_ACCENT_COLOR)));
        sender.sendMessage(Component.text("    Default Minecraft chat code (0-9, a-f, k-o, r)").color(INFO_COLOR));
        sender.sendMessage(Component.text("    `l`cBold and Red " + Emoji.RIGHT + " ").color(INFO_COLOR)
                .append(Component.text("Bold and Red").decorate(TextDecoration.BOLD).color(PaperAdventure.asAdventure(ChatFormatting.RED))));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.empty()
                .append(Component.text("Names: ").decorate(TextDecoration.BOLD).color(INFO_COLOR))
                .append(Component.text("``name").color(INFO_ACCENT_COLOR)));
        sender.sendMessage(Component.text("    Full name of the color or format").color(INFO_COLOR));
        sender.sendMessage(Component.text("    ``bold``redBold and Red " + Emoji.RIGHT + " ").color(INFO_COLOR)
                .append(Component.text("Bold and Red").decorate(TextDecoration.BOLD).color(PaperAdventure.asAdventure(ChatFormatting.RED))));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("/formatting <codes|names> for lists").color(INFO_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.add("codes");
            out.add("names");
        }

        return out;
    }
}
