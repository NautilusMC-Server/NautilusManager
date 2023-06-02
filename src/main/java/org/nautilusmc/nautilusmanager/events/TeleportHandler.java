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
import java.util.function.Supplier;

public class TeleportHandler implements Listener {

    private static final Map<UUID, Location> lastTeleportLocs = new HashMap<>();

    private static final Map<UUID, Map.Entry<Location, BukkitRunnable>> teleporting = new HashMap<>(); // map of Player to starting Location and Runnable that is going to teleport them

    public static Location getLastTeleportLocation(Player player) {
        return lastTeleportLocs.get(player.getUniqueId());
    }

    public static void setLastTeleportLoc(Player player, Location loc) {
        lastTeleportLocs.put(player.getUniqueId(), loc);
    }

    public static void teleportAfterDelay(Player player, Location loc, int ticks) {
        teleportAfterDelay(player, () -> loc, ticks, null);
    }

    public static void teleportAfterDelay(Player player, Supplier<Location> loc, int ticks, Runnable failCallback) {
        TextColor color = TextColor.color(255, 194, 0);

        player.sendMessage(Component.text("Teleporting in "+Math.round(ticks/20f)+" seconds. Don't move!").color(color));

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("Whoosh!").color(color));
                setLastTeleportLoc(player, player.getLocation());
                player.teleport(loc.get());

                teleporting.remove(player.getUniqueId());
            }

            @Override
            public void cancel() {
                player.sendMessage(Component.text("Teleport canceled!").color(NautilusCommand.ERROR_COLOR));
                teleporting.remove(player.getUniqueId());
                super.cancel();

                if (failCallback != null) failCallback.run();
            }
        };
        runnable.runTaskLater(NautilusManager.INSTANCE, ticks);
        teleporting.put(player.getUniqueId(), Map.entry(player.getLocation(), runnable));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Map.Entry<Location, BukkitRunnable> entry = teleporting.get(e.getPlayer().getUniqueId());
        if (entry == null ||
                entry.getKey().getWorld().equals(e.getPlayer().getWorld()) &&
                        entry.getKey().distanceSquared(e.getPlayer().getLocation()) < 0.01) return;

        entry.getValue().cancel();
    }
}
