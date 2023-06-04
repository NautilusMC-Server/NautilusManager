package org.nautilusmc.nautilusmanager.util;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.Default;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.ErrorMessage;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.Permission;

import java.util.*;

public class ConfirmationMessage {
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
                .append(taskName).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text("?"))
                .color(Default.INFO_COLOR));
        player.sendMessage(Util.clickableCommand("/confirm", true).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text(" to confirm"))
                .color(Default.INFO_COLOR));
        player.sendMessage(Util.clickableCommand("/deny", true).color(Default.INFO_ACCENT_COLOR)
                .append(Component.text(" to deny"))
                .color(Default.INFO_COLOR));

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
            player.sendMessage(ErrorMessage.NO_PENDING_CONFIRM);
            return;
        }

        Stack<ConfirmationMessage> stack = PENDING.get(uuid);
        stack.pop().execute();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
    }

    public static void deny(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PENDING.containsKey(uuid)) {
            player.sendMessage(ErrorMessage.NO_PENDING_DENY);
            return;
        }

        Stack<ConfirmationMessage> stack = PENDING.get(uuid);
        stack.pop();
        if (stack.isEmpty()) {
            PENDING.remove(uuid);
        }
        player.sendMessage(Component.text("Task denied.").color(Default.INFO_COLOR));
    }
}
