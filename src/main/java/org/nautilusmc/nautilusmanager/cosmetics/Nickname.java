package org.nautilusmc.nautilusmanager.cosmetics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand.ErrorMessage;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.util.CaseInsensitiveString;
import org.nautilusmc.nautilusmanager.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Nickname {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 16;

    private static final BiMap<UUID, CaseInsensitiveString> playerNames = HashBiMap.create();
    private static SQLHandler SQL_HANDLER;

    public static void init() {
        SQL_HANDLER = new SQLHandler("nicknames") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                playerNames.clear();
                while (results.next()) {
                    playerNames.put(UUID.fromString(results.getString("uuid")), new CaseInsensitiveString(results.getString("nickname")));
                }

                // reset any invalid nicknames
                playerNames.forEach((uuid, name) -> {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    if (validateNickname(p, name.string) != null) {
                        setNickname(uuid, null);
                    }
                });

                for (Player p : Bukkit.getOnlinePlayers()) {
                    String nickname = playerNames.getOrDefault(p.getUniqueId(), new CaseInsensitiveString(p.getName())).string;

                    if (!Util.getTextContent(p.displayName()).equals(nickname)) {
                        updateNickname(p, nickname);
                    }
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(new NicknameListener(), NautilusManager.INSTANCE);
    }

    /**
     * Private helper function to update the map in memory and update the SQL database.
     */
    private static void setNickname(UUID uuid, String nickname) {
        if (nickname == null) {
            playerNames.remove(uuid);
            SQL_HANDLER.deleteSQL(uuid);
        }
        else {
            playerNames.put(uuid, new CaseInsensitiveString(nickname));
            SQL_HANDLER.setSQL(uuid, Map.of("nickname", nickname));
        }
    }

    /**
     * Private helper function to actually update a player's nickname in-game.
     */
    private static void updateNickname(Player player, String name) {
        player.displayName(Component.text(name));

        NameColor nameColor = NameColor.getNameColor(player);
        if (nameColor != null) {
            NameColor.updateNameColor(player, nameColor);
        }

        Util.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers());
    }

    /**
     * Get a player's nickname (inverse of #getPlayerFromNickname).
     */
    public static String getNickname(OfflinePlayer p) {
        return playerNames.getOrDefault(p.getUniqueId(), new CaseInsensitiveString(null)).string;
    }

    /**
     * Get a player by nickname (inverse of #getNickname).
     */
    public static OfflinePlayer getPlayerFromNickname(String nickname) {
        CaseInsensitiveString cis = new CaseInsensitiveString(nickname);
        if (!playerNames.containsValue(cis)) return null;

        String name = Bukkit.getOfflinePlayer(playerNames.inverse().get(cis)).getName();
        if (name == null) return null;

        return Util.getOfflinePlayerIfCached(name);
    }

    /**
     * Get a list of all nicknames.
     */
    public static List<String> getNicknames() {
        return playerNames.values().stream().map(name -> name.string).toList();
    }

    /**
     * Set a player's nickname.
     */
    public static void setNickname(Player p, String name, boolean sendMessage) {
        if (!name.equals(getNickname(p))) {
            setNickname(p.getUniqueId(), name.equals(p.getName()) ? null : name);
            updateNickname(p, name);
        }

        if (sendMessage) p.sendMessage(Component.text("Nickname set to ").append(p.displayName()));
    }

    /**
     * Make sure a given nickname is valid based on requirements.
     */
    public static String validateNickname(OfflinePlayer player, String name) {
        if (name.length() > MAX_LENGTH)
            return "That nickname is too long! Maximum length is " + MAX_LENGTH + ".";
        if (name.length() < MIN_LENGTH)
            return "That nickname is too short! Minimum length is " + MIN_LENGTH + ".";
        if (name.contains(" "))
            return "Nicknames cannot contain spaces!";
        if (!name.matches("[a-zA-Z0-9_]+") && player instanceof Player onlinePlayer && !onlinePlayer.hasPermission(NautilusCommand.Permission.NICKNAME_SPECIAL_CHARS))
            return "Become a supporter to unlock non-alphanumeric characters!";

        OfflinePlayer existing = Util.getOfflinePlayerIfCached(name);
        if (!playerNames.inverse().getOrDefault(new CaseInsensitiveString(name),
                player.getUniqueId()).equals(player.getUniqueId()) || (existing != null && !existing.getUniqueId().equals(player.getUniqueId())))
            return "That nickname is already taken!";

        return null;
    }

    public static class NicknameListener implements Listener {
        @EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
        public void onPlayerJoin(PlayerJoinEvent e) {
            String nickname = getNickname(e.getPlayer());

            // if there is an online player whose nick is the joining player's name, reset the player's nick
            // otherwise, it will be reset next time that player joins
            OfflinePlayer player = getPlayerFromNickname(e.getPlayer().getName());
            if (player instanceof Player onlinePlayer) {
                setNickname(onlinePlayer, onlinePlayer.getName(), false);
                onlinePlayer.sendMessage(ErrorMessage.NICKNAME_CONFLICT_RESET);
            }

            // if there is a player whose name is the joining player's nickname, reset the joining player's nick
            // otherwise, update the player's nickname as usual
            if (Util.getOfflinePlayerIfCached(nickname) != null) {
                setNickname(e.getPlayer(), e.getPlayer().getName(), false);
                e.getPlayer().sendMessage(ErrorMessage.NICKNAME_CONFLICT_RESET);
            } else {
                updateNickname(e.getPlayer(), nickname);
            }
        }
    }
}
