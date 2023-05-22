package org.nautilusmc.nautilusmanager.events;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.*;

public class AfkManager implements Listener {

    private static final Map<UUID, Long> AFK = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> AFK_TIMERS = new HashMap<>();

    public static long getTimeAfk(Player player) {
        long timeAFKed = AFK.getOrDefault(player.getUniqueId(), -1L);
        if (timeAFKed == -1) return -1;

        return (System.currentTimeMillis() - timeAFKed)/1000L;
    }

    public static boolean isAfk(Player player) {
        return AFK.containsKey(player.getUniqueId());
    }

    public static void toggleAFK(Player player) {
        setAFK(player, !isAfk(player));
    }

    public static void setAFK(Player player, boolean afk) {
        if (isAfk(player) == afk) return;

        String verb;

        if (afk) {
            AFK.put(player.getUniqueId(), System.currentTimeMillis());
            verb = "now";
            removeTimer(player);

            Bukkit.getScheduler().runTaskLater(NautilusManager.INSTANCE, () -> {
                if (isAfk(player)) {
                    ((CraftPlayer) player).getHandle().fauxSleeping = true;
                    ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().updateSleepingPlayerList();
                }
            }, NautilusManager.INSTANCE.getConfig().getInt("afk.timeToIgnoreSleep") * 20L);
        } else {
            AFK.remove(player.getUniqueId());
            verb = "no longer";
            resetTimer(player);
        }

        player.sendMessage(Component.text("You are "+verb+" AFK.")
                .color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == player) continue;
            p.sendMessage(Component.text("* ")
                    .append(Util.modifyColor(player.displayName(), -30, -30, -30))
                    .append(Component.text(" is "+verb+" AFK"))
                    .color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
        }
    }

    private static void removeTimer(Player player) {
        BukkitRunnable timer = AFK_TIMERS.get(player.getUniqueId());
        if (timer != null) timer.cancel();
    }

    private static void resetTimer(Player player) {
        UUID uuid = player.getUniqueId();
        removeTimer(player);

        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                setAFK(Bukkit.getPlayer(uuid), true);
            }
        };
        timer.runTaskLater(NautilusManager.INSTANCE, NautilusManager.INSTANCE.getConfig().getInt("afk.timeToAfk") * 20L);

        AFK_TIMERS.put(uuid, timer);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        resetTimer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        setAFK(e.getPlayer(), false);
        removeTimer(e.getPlayer());
    }

    // move or change orientation
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        setAFK(e.getPlayer(), false);
    }

    @EventHandler
    public void onInteract(PlayerArmSwingEvent e) {
        setAFK(e.getPlayer(), false);
    }

    @EventHandler
    public void onInventoryClick(InventoryInteractEvent e) {
        if (e.getWhoClicked() instanceof Player player) setAFK(player, false);
    }
}
