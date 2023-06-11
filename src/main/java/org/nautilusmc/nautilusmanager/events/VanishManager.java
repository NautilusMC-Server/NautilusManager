package org.nautilusmc.nautilusmanager.events;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VanishManager implements Listener {
    private static final List<UUID> VANISHED_PLAYERS = new ArrayList<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        VANISHED_PLAYERS.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (UUID uuid : VANISHED_PLAYERS) {
            setVanished(Bukkit.getPlayer(uuid), e.getPlayer(), true);
        }
    }

    public static void unload() {
        for (int i = VANISHED_PLAYERS.size() - 1; i >= 0; i--) {
            Player player = Bukkit.getPlayer(VANISHED_PLAYERS.get(i));
            if (player == null) continue;
            player.sendMessage(Component.text("Plugin unloaded, you are now visible").color(Command.ERROR_COLOR));
            unvanish(player);
        }
    }

    private static void setVanished(Player vanisher, Player player, boolean vanished) {
        if (!vanished) {
            player.showPlayer(NautilusManager.INSTANCE, vanisher);
        } else if (!player.hasPermission(Permission.VANISH.toString())) {
            player.hidePlayer(NautilusManager.INSTANCE, vanisher);
        }
    }

    public static void vanish(Player player) {
        VANISHED_PLAYERS.add(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> setVanished(player, onlinePlayer, true));
    }

    public static void unvanish(Player player) {
        VANISHED_PLAYERS.remove(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> setVanished(player, onlinePlayer, false));
        Util.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers());
    }

    public static boolean isVanished(Player player) {
        return VANISHED_PLAYERS.contains(player.getUniqueId());
    }
}
