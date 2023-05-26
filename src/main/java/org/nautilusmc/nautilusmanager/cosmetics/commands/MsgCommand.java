package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.events.AfkManager;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MsgCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 2) return false;

        Player recipient = Util.getOnlinePlayer(strings[0]);
        if (recipient == null) {
            commandSender.sendMessage(Component.text("Player not found").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));

        Component msg = MessageStyler.formatUserMessage(commandSender, Component.text(message))
                .color(NautilusManager.DEFAULT_CHAT_TEXT_COLOR)
                .decorate(TextDecoration.ITALIC);

        if (commandSender instanceof Player player) {
            ReplyCommand.messaged(player.getUniqueId(), recipient.getUniqueId());
        }

        if (AfkManager.isAfk(recipient)) {
            recipient.playSound(recipient.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 2);
            Bukkit.getScheduler().scheduleSyncDelayedTask(NautilusManager.INSTANCE, () -> {
                recipient.playSound(recipient.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 4F);
            }, 2);
        }

        recipient.sendMessage(Component.empty()
                .append(MessageStyler.getTimeStamp())
                .append(Component.text(" "))
                .append((commandSender instanceof Player p ? p.displayName() : commandSender.name()).decorate(TextDecoration.ITALIC))
                .append(Component.text(" whispered to you").color(TextColor.color(150, 150, 150)).decorate(TextDecoration.ITALIC))
                .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                .append(msg)
        );
        commandSender.sendMessage(Component.empty()
                .append(MessageStyler.getTimeStamp())
                .append(Component.text(" You whispered to ").color(TextColor.color(150, 150, 150)).decorate(TextDecoration.ITALIC))
                .append(recipient.displayName().decorate(TextDecoration.ITALIC))
                .append(Component.text(" » ").color(TextColor.color(150, 150, 150)))
                .append(msg)
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map(Util::getName).toList());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
