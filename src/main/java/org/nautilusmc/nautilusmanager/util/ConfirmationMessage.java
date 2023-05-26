package org.nautilusmc.nautilusmanager.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;

import java.util.*;

public class ConfirmationMessage {
    private Player player;
    private BukkitRunnable runnable;
    private String taskName;
    private static HashMap<UUID, Stack<ConfirmationMessage>> PENDING = new HashMap<>();
    private ConfirmationMessage(Player player, BukkitRunnable runnable) {
        this.player = player;
        this.runnable = runnable;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public BukkitRunnable getRunnable() {
        return runnable;
    }

    public void setRunnable(BukkitRunnable runnable) {
        this.runnable = runnable;
    }
    public void execute() {
        runnable.runTask(NautilusManager.INSTANCE);
    }

    public static void sendConfirmationMessage(Player player, Component taskName, BukkitRunnable runnable) {
        player.sendMessage(Component.text("Are you sure you want to ").color(NautilusCommand.MAIN_COLOR)
                .append(taskName)
                .append(Component.text("?").color(NautilusCommand.MAIN_COLOR)));

        player.sendMessage(Util.clickableCommand("/confirm", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text(" to confirm").color(NautilusCommand.MAIN_COLOR)));

        player.sendMessage(Util.clickableCommand("/deny", true).color(NautilusCommand.ACCENT_COLOR)
                .append(Component.text(" to confirm").color(NautilusCommand.MAIN_COLOR)));

        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            PENDING.put(uuid, new Stack<>());
        }
        Stack<ConfirmationMessage> stack = PENDING.get(uuid);
        if (stack.size() > 10) {
            stack.remove(stack.firstElement());
        }
        PENDING.get(uuid).push(new ConfirmationMessage(player, runnable));
    }

    public static void confirm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(Component.text("No pending tasks to confirm!").color(NautilusCommand.ERROR_COLOR));
            return;
        }
        Stack<ConfirmationMessage> stack =  PENDING.get(uuid);
        stack.pop().execute();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
    }
    public static void deny(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(Component.text("No pending tasks to deny!").color(NautilusCommand.ERROR_COLOR));
            return;
        }
        Stack<ConfirmationMessage> stack =  PENDING.get(uuid);
        stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
        player.sendMessage(Component.text("Task denied!").color(NautilusCommand.ERROR_COLOR));
    }
}
