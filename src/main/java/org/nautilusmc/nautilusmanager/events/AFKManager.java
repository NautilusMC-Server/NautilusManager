package org.nautilusmc.nautilusmanager.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager implements Listener {
    private static final Map<UUID, Long> AFK_START_MILLIS = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> AFK_TIMERS = new HashMap<>();

    public static long getSecondsAfk(Player player) {
        long afkStartMillis = AFK_START_MILLIS.getOrDefault(player.getUniqueId(), -1L);
        if (afkStartMillis == -1) return -1;

        return (System.currentTimeMillis() - afkStartMillis) / 1000L;
    }

    public static boolean isAFK(Player player) {
        return AFK_START_MILLIS.containsKey(player.getUniqueId());
    }

    public static void toggleAFK(Player player) {
        setAFK(player, !isAFK(player));
    }

    public static void setAFK(Player player, boolean afk) {
        if (!afk) resetTimer(player);
        if (isAFK(player) == afk) return;

        String status;

        if (afk) {
            AFK_START_MILLIS.put(player.getUniqueId(), System.currentTimeMillis());
            status = "now AFK";
            removeTimer(player);

            Bukkit.getScheduler().runTaskLater(NautilusManager.getPlugin(), () -> {
                if (isAFK(player)) {
                    player.setSleepingIgnored(true);
                }
            }, NautilusManager.getPlugin().getConfig().getInt("afk.timeToIgnoreSleep") * (long) Util.TICKS_PER_SECOND);
        } else {
            AFK_START_MILLIS.remove(player.getUniqueId());
            status = "no longer AFK";

            player.setSleepingIgnored(false);
        }

        player.sendMessage(Component.empty()
                .append(MessageStyler.getTimeStamp())
                .appendSpace()
                .append(Component.text("* You are " + status + "."))
                .color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
        Component message = Component.empty()
                .append(MessageStyler.getTimeStamp())
                .appendSpace()
                .append(Component.text("* ", NamedTextColor.GRAY, TextDecoration.ITALIC))
                .append(player.displayName())
                .append(Component.text(" is " + status + "."))
                .color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC);
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient != player) {
                recipient.sendMessage(message);
            }
        }
    }

    private static void removeTimer(Player player) {
        BukkitRunnable timer = AFK_TIMERS.get(player.getUniqueId());
        if (timer != null) timer.cancel();

        AFK_TIMERS.remove(player.getUniqueId());
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
        timer.runTaskLater(NautilusManager.getPlugin(), NautilusManager.getPlugin().getConfig().getInt("afk.timeToAfk") * 20L);

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

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.hasChangedOrientation()) {
            setAFK(e.getPlayer(), false);
        }
    }

    @EventHandler
    public void onInteract(PlayerArmSwingEvent e) {
        setAFK(e.getPlayer(), false);
    }

    @EventHandler
    public void onInventoryClick(InventoryInteractEvent e) {
        if (e.getWhoClicked() instanceof Player player) setAFK(player, false);
    }

    // high priority so the "no longer AFK" message appears before the message itself
    @EventHandler(priority = EventPriority.HIGH)
    public void onMessage(AsyncChatEvent e) {
        setAFK(e.getPlayer(), false);
    }
}
