package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SponsorCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        commandSender.sendMessage(Component.text("------- ")
                .append(Component.text("Sponsor Perks").color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" -------"))
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.text("Chat formatting")
                .color(NautilusCommand.ACCENT_COLOR)
                .decorate(TextDecoration.BOLD));
        commandSender.sendMessage(Component.text(" - ")
                .append(Component.text("Colors").color(NamedTextColor.AQUA))
                .append(Component.text(" and formatting ("))
                .append(Component.text("bold").decorate(TextDecoration.BOLD))
                .append(Component.text(", "))
                .append(Component.text("italic").decorate(TextDecoration.ITALIC))
                .append(Component.text(", etc.)"))
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.text(" - Works in global chat, private messages, and anvils!")
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.text(" - ")
                .append(Util.clickableCommand("/formatting", true).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" to learn more"))
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.empty());

        commandSender.sendMessage(Component.text("Bonus Name Colors")
                .color(NautilusCommand.ACCENT_COLOR)
                .decorate(TextDecoration.BOLD));
        commandSender.sendMessage(Component.text(" - Extra name color options like ")
                .append(FancyText.ColorType.ALTERNATING.exampleItem().getItemMeta().displayName())
                .append(Component.text(" and "))
                .append(FancyText.ColorType.GRADIENT.exampleItem().getItemMeta().displayName())
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.text(" - ")
                .append(Util.clickableCommand("/cosmetics menu", true).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" to view"))
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.empty());

        commandSender.sendMessage(Component.text("Special Characters in Nicknames")
                .color(NautilusCommand.ACCENT_COLOR)
                .decorate(TextDecoration.BOLD));
        commandSender.sendMessage(Component.text(" - Non-ascii characters in nicknames such as ☺")
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.text(" - ")
                .append(Util.clickableCommand("/cosmetics menu", true).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" or "))
                .append(Util.clickableCommand("/nickname set ", false).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text("to set it"))
                .color(NautilusCommand.MAIN_COLOR));
        commandSender.sendMessage(Component.empty());

        String url = "https://store.nautilusmc.org";
        commandSender.sendMessage(Component.text("Support NautilusMC today at ")
                .append(MessageStyler.styleURL(Component.text(url), url))
                .append(Component.text("!"))
                .color(NautilusCommand.ACCENT_COLOR));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
