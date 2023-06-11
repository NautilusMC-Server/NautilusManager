package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Util;

public class SponsorCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage(Component.text("------- ")
                .append(Component.text("Sponsor Perks").color(INFO_ACCENT_COLOR))
                .append(Component.text(" -------"))
                .color(INFO_COLOR));
        sender.sendMessage(Component.text("Chat formatting")
                .color(INFO_ACCENT_COLOR)
                .decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text(" - ")
                .append(Component.text("Colors").color(NamedTextColor.AQUA))
                .append(Component.text(" and formatting ("))
                .append(Component.text("bold").decorate(TextDecoration.BOLD))
                .append(Component.text(", "))
                .append(Component.text("italic").decorate(TextDecoration.ITALIC))
                .append(Component.text(", etc.)"))
                .color(INFO_COLOR));
        sender.sendMessage(Component.text(" - Works in global chat, private messages, and anvils!")
                .color(INFO_COLOR));
        sender.sendMessage(Component.text(" - ")
                .append(Util.clickableCommand("/formatting", true).color(INFO_ACCENT_COLOR))
                .append(Component.text(" to learn more"))
                .color(INFO_COLOR));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.text("Bonus Name Colors")
                .color(INFO_ACCENT_COLOR)
                .decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text(" - Extra name color options like ")
                .append(FancyText.ColorType.ALTERNATING.exampleItem().getItemMeta().displayName())
                .append(Component.text(" and "))
                .append(FancyText.ColorType.GRADIENT.exampleItem().getItemMeta().displayName())
                .color(INFO_COLOR));
        sender.sendMessage(Component.text(" - ")
                .append(Util.clickableCommand("/cosmetics menu", true).color(INFO_ACCENT_COLOR))
                .append(Component.text(" to view"))
                .color(INFO_COLOR));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.text("Special Characters in Nicknames")
                .color(INFO_ACCENT_COLOR)
                .decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text(" - Non-ascii characters in nicknames such as ☺")
                .color(INFO_COLOR));
        sender.sendMessage(Component.text(" - ")
                .append(Util.clickableCommand("/cosmetics menu", true).color(INFO_ACCENT_COLOR))
                .append(Component.text(" or "))
                .append(Util.clickableCommand("/nickname set ", false).color(INFO_ACCENT_COLOR))
                .append(Component.text("to set it"))
                .color(INFO_COLOR));
        sender.sendMessage(Component.empty());

        String url = "https://nautilusmc.tebex.io";
        sender.sendMessage(Component.text("Support NautilusMC today at ")
                .append(MessageStyler.styleURL(Component.text(url), url))
                .append(Component.text("!"))
                .color(INFO_ACCENT_COLOR));

        return true;
    }
}
