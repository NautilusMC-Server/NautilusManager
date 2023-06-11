package org.nautilusmc.nautilusmanager.events;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class TeleportHandler implements Listener {

    public static final int DEFAULT_TELEPORT_DELAY_TICKS = 5 * 20;

    // player : previous location
    private static final Map<UUID, Location> PREVIOUS_LOCATIONS = new HashMap<>();
    // player : (starting location, teleporter)
    private static final Map<UUID, Map.Entry<Location, BukkitRunnable>> TELEPORT_INFO = new HashMap<>();

    public static Location getLastTeleportLocation(Player player) {
        return PREVIOUS_LOCATIONS.get(player.getUniqueId());
    }

    public static void setLastTeleportLoc(Player player, Location loc) {
        PREVIOUS_LOCATIONS.put(player.getUniqueId(), loc);
    }

    public static void teleportAfterDelay(Player player, Location destination) {
        teleportAfterDelay(player, () -> destination, DEFAULT_TELEPORT_DELAY_TICKS);
    }

    public static void teleportAfterDelay(Player player, Supplier<Location> destination, int tickDelay) {
        teleportAfterDelay(player, destination, tickDelay, null);
    }

    public static void teleportAfterDelay(Player player, Supplier<Location> destination, int tickDelay, Runnable failCallback) {
        player.sendMessage(Component.text("Teleporting in ")
                .append(Component.text(Math.round(tickDelay / 20f)))
                .append(Component.text(" seconds. Don't move!"))
                .color(Command.INFO_COLOR));

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(Component.text("Whoosh!").color(Command.INFO_COLOR));
                setLastTeleportLoc(player, player.getLocation());
                player.teleport(destination.get());

                TELEPORT_INFO.remove(player.getUniqueId());
            }

            @Override
            public void cancel() {
                player.sendMessage(Component.text("Teleport canceled; you moved!").color(Command.ERROR_COLOR));
                TELEPORT_INFO.remove(player.getUniqueId());
                super.cancel();

                if (failCallback != null) failCallback.run();
            }
        };
        runnable.runTaskLater(NautilusManager.INSTANCE, tickDelay);
        TELEPORT_INFO.put(player.getUniqueId(), Map.entry(player.getLocation(), runnable));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Map.Entry<Location, BukkitRunnable> info = TELEPORT_INFO.get(e.getPlayer().getUniqueId());
        if (info == null || (info.getKey().getWorld().equals(e.getPlayer().getWorld()) &&
                        info.getKey().distanceSquared(e.getPlayer().getLocation()) < 0.01)) return;

        info.getValue().cancel();
    }
}
