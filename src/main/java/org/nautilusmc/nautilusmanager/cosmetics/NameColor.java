package org.nautilusmc.nautilusmanager.cosmetics;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.sql.SQLListener;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameColor {

    public static final NameColor DEFAULT_COLOR = new NameColor(FancyText.ColorType.SOLID, TextColor.color(255, 255, 255));

    private static final Map<UUID, NameColor> playerColors = new HashMap<>();
    private static SQLListener SQL_LISTENER;

    public static void init() {
        SQL_LISTENER = new SQLListener("name_colors") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                Map<UUID, NameColor> newColors = new HashMap<>();
                while (results.next()) {
                    FancyText.ColorType type = FancyText.ColorType.values()[results.getInt("color_type")];
                    TextColor[] colors = new TextColor[type.numColors];

                    for (int i = 0; i < type.numColors; i++) {
                        colors[i] = TextColor.color(results.getInt("color" + (i + 1)));
                    }

                    newColors.put(UUID.fromString(results.getString("uuid")), new NameColor(type, colors));
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    NameColor oldColor = playerColors.getOrDefault(p.getUniqueId(), DEFAULT_COLOR);
                    NameColor newColor = newColors.getOrDefault(p.getUniqueId(), DEFAULT_COLOR);

                    if (!oldColor.equals(newColor)) {
                        updateNameColor(p, newColor);
                    }
                }

                playerColors.clear();
                playerColors.putAll(newColors);
            }
        };

        Bukkit.getPluginManager().registerEvents(new NameColorListener(), NautilusManager.INSTANCE);
    }

    public static NameColor getNameColor(OfflinePlayer player) {
        return playerColors.containsKey(player.getUniqueId()) ? playerColors.get(player.getUniqueId()).copy() : null;
    }

    public final FancyText.ColorType type;
    public final TextColor[] colors;

    private NameColor copy() {
        return new NameColor(type, colors);
    }

    private NameColor(FancyText.ColorType type, TextColor... colors) {
        this.type = type;
        this.colors = colors;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NameColor color && color.type == type && Arrays.equals(color.colors, colors);
    }

    private static void setNameColor(UUID uuid, NameColor color) {
        if (color == null) {
            playerColors.remove(uuid);
            SQL_LISTENER.deleteSQL(uuid);
        }
        else {
            playerColors.put(uuid, color);

            Map<String, Object> values = new HashMap<>();
            values.put("color_type", color.type.ordinal());
            for (int i = 0; i < color.type.numColors; i++) values.put("color" + (i + 1), color.colors[i].value());
            SQL_LISTENER.setSQL(uuid, values);
        }
    }

    public static void setNameColor(Player player, FancyText.ColorType type, boolean sendMessage, TextColor... colors) {
        setNameColor(player, sendMessage, new NameColor(type, colors));
    }

    public static void setNameColor(Player player, boolean sendMessage, NameColor color) {
        if (!color.equals(getNameColor(player))) {
            updateNameColor(player, color);
            setNameColor(player.getUniqueId(), color.equals(DEFAULT_COLOR) ? null : color);
        }

        if (sendMessage) player.sendMessage(FancyText.colorText(color.type, "Name color changed", color.colors));
    }

    public static void updateNameColor(Player player, NameColor color) {
        player.displayName(FancyText.colorText(color.type, Util.getTextContent(player.displayName()), color.colors));
        Util.updateNameTag(player, player.displayName(), Bukkit.getOnlinePlayers());
    }

    public static class NameColorListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            NameColor color = getNameColor(e.getPlayer());
            if (color != null && !color.equals(DEFAULT_COLOR)) updateNameColor(e.getPlayer(), color);
        }
    }
}
