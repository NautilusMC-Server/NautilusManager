package org.nautilusmc.nautilusmanager.util;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;

import java.util.*;

public class ConfirmationMessage {
    public static final Component NO_PENDING_CONFIRM_ERROR = Component.text("No pending tasks to confirm!").color(Command.ERROR_COLOR);
    public static final Component NO_PENDING_DENY_ERROR = Component.text("No pending tasks to deny!").color(Command.ERROR_COLOR);

    private static final HashMap<UUID, Stack<ConfirmationMessage>> PENDING = new HashMap<>();

    private Player player;
    private BukkitRunnable runnable;
    private String taskName;

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
        player.sendMessage(Component.text("Are you sure you want to ")
                .append(taskName).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text("?"))
                .color(Command.INFO_COLOR));
        player.sendMessage(Util.clickableCommand("/confirm", true).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" to confirm"))
                .color(Command.INFO_COLOR));
        player.sendMessage(Util.clickableCommand("/deny", true).color(Command.INFO_ACCENT_COLOR)
                .append(Component.text(" to deny"))
                .color(Command.INFO_COLOR));

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
            player.sendMessage(NO_PENDING_CONFIRM_ERROR);
            return;
        }

        Stack<ConfirmationMessage> stack = PENDING.get(uuid);
        stack.pop().execute();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }

        player.sendMessage(Component.text("Task confirmed.").color(Command.INFO_COLOR));
    }

    public static void deny(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(NO_PENDING_DENY_ERROR);
            return;
        }

        Stack<ConfirmationMessage> stack = PENDING.get(uuid);
        stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }

        player.sendMessage(Component.text("Task denied.").color(Command.INFO_COLOR));
    }
}
