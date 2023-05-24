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
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class VanishManager implements Listener {

    private static final List<UUID> vanishedPlayers = new ArrayList<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        vanishedPlayers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (UUID uuid : vanishedPlayers) {
            setVanished(Bukkit.getPlayer(uuid), e.getPlayer(), true);
        }
    }

    public static void unload() {
        for (int i = vanishedPlayers.size()-1; i >= 0; i--) {
            Player player = Bukkit.getPlayer(vanishedPlayers.get(i));
            if (player == null) continue;
            player.sendMessage(Component.text("Plugin unloaded, you are now visible").color(NautilusCommand.ERROR_COLOR));
            unvanish(player);
        }
    }

    private static void setVanished(Player vanisher, Player player, boolean vanished) {
        if (!vanished) {
            player.showPlayer(NautilusManager.INSTANCE, vanisher);
        } else if (!player.hasPermission(NautilusCommand.VANISH_PERM)) {
            player.hidePlayer(NautilusManager.INSTANCE, vanisher);
        }
    }

    public static void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> setVanished(player, onlinePlayer, true));
    }

    public static void unvanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> setVanished(player, onlinePlayer, false));
        Util.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers());
    }

    public static boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }
}
