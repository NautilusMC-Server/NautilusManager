package org.nautilusmc.nautilusmanager.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportHandler implements Listener {

    private static final Map<UUID, BukkitRunnable> teleporting = new HashMap<>();

    public static void teleportAfterDelay(Player player, Location loc, int ticks) {
        TextColor color = TextColor.color(255, 194, 0);

        player.sendMessage(Component.text("Teleporting in "+Math.round(ticks/20f)+" seconds. Don't move!").color(color));

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("Teleporting...").color(color));
                player.teleport(loc);
            }

            @Override
            public void cancel() {
                player.sendMessage(Component.text("Teleport canceled!").color(NautilusCommand.ERROR_COLOR));
                super.cancel();
            }
        };
        runnable.runTaskLater(NautilusManager.INSTANCE, ticks);
        teleporting.put(player.getUniqueId(), runnable);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        BukkitRunnable runnable;
        if ((runnable = teleporting.get(e.getPlayer().getUniqueId())) == null) return;

        runnable.cancel();
    }
}
