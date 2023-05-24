package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;

import java.util.*;

public class ReplyCommand extends NautilusCommand {

    private static final Map<UUID, UUID> lastMessager = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> runnables = new HashMap<>();
    public static final int LAST_MESSAGER_TIMEOUT = 60; //seconds

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text("Only players can use this command").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        if (strings.length == 0) return false;

        Player player = (Player) commandSender;

        if (!lastMessager.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You have no one to reply to").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        OfflinePlayer recipient = Bukkit.getOfflinePlayer(lastMessager.get(player.getUniqueId()));
        if (!recipient.isOnline()) {
            player.sendMessage(Component.text(recipient.getName()+" is no longer online").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        player.performCommand("msg " + recipient.getName() + " " + String.join(" ", strings));
        return true;
    }

    private static void updateLastMessage(UUID p1, UUID p2) {
        lastMessager.put(p1, p2);
        if (runnables.containsKey(p1)) {
            runnables.get(p1).cancel();
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    lastMessager.remove(p1);
                    runnables.remove(p1);
                }
            };
            runnable.runTaskLater(NautilusManager.INSTANCE, LAST_MESSAGER_TIMEOUT * 20L);
            runnables.put(p1, runnable);
        }
    }

    public static void messaged(UUID sender, UUID receiver) {
        updateLastMessage(sender, receiver);
        updateLastMessage(receiver, sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
