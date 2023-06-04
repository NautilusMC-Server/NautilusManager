package org.nautilusmc.nautilusmanager.cosmetics.commands;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FormattingCommand extends NautilusCommand {
    private static final int PAGE_SIZE = 10;
    private static final String EXAMPLE_STRING = "NautilusMC";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission(Permission.USE_CHAT_FORMATTING)) {
            commandSender.sendMessage(ErrorMessage.NOT_SPONSOR);
            return true;
        }

        if (strings.length > 0) {
            boolean listNames = strings[0].equalsIgnoreCase("names");
            if (!strings[0].equalsIgnoreCase("codes") && !listNames) return false;

            // TODO: abstract this list behavior
            int pageCount = (int) Math.ceil((double) ChatFormatting.values().length / PAGE_SIZE);
            int page = strings.length > 1 ? Math.max(Math.min(Integer.parseInt(strings[1]), pageCount), 1) : 1;

            commandSender.sendMessage(Component.empty()
                    .append(Component.text("----- "))
                    .append(Component.text(WordUtils.capitalizeFully(strings[0])))
                    .append(Component.text(" (Page " + page + "/" + pageCount + ") -----"))
                    .color(Default.INFO_COLOR).decorate(TextDecoration.BOLD));

            for (int i = 0; i < PAGE_SIZE; i++) {
                int idx = (page - 1) * PAGE_SIZE + i;
                if (idx >= ChatFormatting.values().length) break;

                ChatFormatting formatting = ChatFormatting.values()[idx];
                String string = strings[0].equalsIgnoreCase("codes") ? "`" + formatting.getChar() : "``" + formatting.getName().toLowerCase();

                Component message = Component.empty()
                        .append(Component.text(" - "))
                        .append(Component.text(string).color(Default.INFO_ACCENT_COLOR))
                        .append(Component.text(" " + Emoji.RIGHT.getRaw() + " "))
                        .append(Util.nmsFormat(Component.text(EXAMPLE_STRING), formatting))
                        .color(Default.INFO_COLOR);
                commandSender.sendMessage(message);
            }

            if (strings[0].equalsIgnoreCase("codes")) {
                return true;
            } else if (strings[0].equalsIgnoreCase("names")) {
                // TODO: implement
                // xarkenz note: what?
                return true;
            }
        }

        commandSender.sendMessage(Component.empty()
                .append(Component.text("Codes: ").color(Default.INFO_COLOR).decorate(TextDecoration.BOLD))
                .append(Component.text("`x").color(Default.INFO_ACCENT_COLOR)));
        commandSender.sendMessage(Component.text("    Default Minecraft chat code (0-9, a-f, k-o, r)").color(Default.INFO_COLOR));
        commandSender.sendMessage(Component.text("    `l`cBold and Red " + Emoji.RIGHT.getRaw() + " ").color(Default.INFO_COLOR)
                .append(Component.text("Bold and Red").decorate(TextDecoration.BOLD).color(PaperAdventure.asAdventure(ChatFormatting.RED))));
        commandSender.sendMessage(Component.empty());
        commandSender.sendMessage(Component.empty()
                .append(Component.text("Names: ").decorate(TextDecoration.BOLD).color(Default.INFO_COLOR))
                .append(Component.text("``name").color(Default.INFO_ACCENT_COLOR)));
        commandSender.sendMessage(Component.text("    Full name of the color or format").color(Default.INFO_COLOR));
        commandSender.sendMessage(Component.text("    ``bold``redBold and Red " + Emoji.RIGHT.getRaw() + " ").color(Default.INFO_COLOR)
                .append(Component.text("Bold and Red").decorate(TextDecoration.BOLD).color(PaperAdventure.asAdventure(ChatFormatting.RED))));
        commandSender.sendMessage(Component.empty());
        commandSender.sendMessage(Component.text("/formatting <codes|names> for lists").color(Default.INFO_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.add("codes");
            out.add("names");
        }

        return out.stream().filter(s1 -> s1.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
