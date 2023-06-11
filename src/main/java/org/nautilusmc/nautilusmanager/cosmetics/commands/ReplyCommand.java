package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;

import java.util.*;

public class ReplyCommand extends Command {
    public static final int REPLY_TIMEOUT_SECONDS = 60;

    // reply from : reply to
    private static final Map<UUID, UUID> PLAYER_REPLY_TARGETS = new HashMap<>();
    // reply from : timer
    private static final Map<UUID, BukkitRunnable> PLAYER_REPLY_TIMERS = new HashMap<>();

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (args.length < 1) return false;

        if (!PLAYER_REPLY_TARGETS.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You have no one to reply to!").color(ERROR_COLOR));
            return true;
        }

        OfflinePlayer recipient = Bukkit.getOfflinePlayer(PLAYER_REPLY_TARGETS.get(player.getUniqueId()));
        if (!recipient.isOnline()) {
            String recipientName = Objects.requireNonNullElse(recipient.getName(), "(unknown player)");
            player.sendMessage(Component.empty().append(Component.text(recipientName).color(ERROR_ACCENT_COLOR))
                    .append(Component.text(" is no longer online!").color(ERROR_COLOR)));
            return true;
        }

        player.performCommand("msg " + recipient.getName() + " " + getMessageFromArgs(args, 0));
        return true;
    }

    private static void updateLastMessage(UUID replyFrom, UUID replyTo) {
        PLAYER_REPLY_TARGETS.put(replyFrom, replyTo);
        if (PLAYER_REPLY_TIMERS.containsKey(replyFrom)) {
            PLAYER_REPLY_TIMERS.get(replyFrom).cancel();
            BukkitRunnable replyTimer = new BukkitRunnable() {
                @Override
                public void run() {
                    PLAYER_REPLY_TARGETS.remove(replyFrom);
                    PLAYER_REPLY_TIMERS.remove(replyFrom);
                }
            };
            replyTimer.runTaskLater(NautilusManager.INSTANCE, REPLY_TIMEOUT_SECONDS * 20L);
            PLAYER_REPLY_TIMERS.put(replyFrom, replyTimer);
        }
    }

    public static void update(UUID sender, UUID receiver) {
        updateLastMessage(sender, receiver);
        updateLastMessage(receiver, sender);
    }
}
