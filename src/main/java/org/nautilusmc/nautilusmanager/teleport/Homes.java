package org.nautilusmc.nautilusmanager.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.util.CaseInsensitiveString;
import org.nautilusmc.nautilusmanager.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Homes {
    public static final int MAX_NAME_LENGTH = 16;

    private static final Map<UUID, Map<CaseInsensitiveString, Location>> PLAYER_HOMES = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_HOME_CAPACITIES = new HashMap<>();

    public static SQLHandler HOME_LOCATION_DB;
    public static SQLHandler HOME_CAPACITY_DB;

    public static void init() {
        HOME_LOCATION_DB = new SQLHandler("homes", "uuid_name") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                while (results.next()) {
                    String[] uuidName = results.getString("uuid_name").split("/");
                    UUID uuid = UUID.fromString(uuidName[0]);
                    Location loc = new Location(
                            Bukkit.getWorld(UUID.fromString(results.getString("world"))),
                            results.getDouble("x"),
                            results.getDouble("y"),
                            results.getDouble("z"),
                            results.getFloat("yaw"),
                            results.getFloat("pitch")
                    );

                    if (!PLAYER_HOMES.containsKey(uuid)) PLAYER_HOMES.put(uuid, new HashMap<>());

                    PLAYER_HOMES.get(uuid).put(new CaseInsensitiveString(uuidName[1]), loc);
                }
            }
        };

        HOME_CAPACITY_DB = new SQLHandler("home_amounts", "uuid") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                while (results.next()) {
                    PLAYER_HOME_CAPACITIES.put(UUID.fromString(results.getString("uuid")), results.getInt("amount"));
                }
            }
        };
    }

    public static Location getHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        return !PLAYER_HOMES.containsKey(uuid) ? null : PLAYER_HOMES.get(uuid).get(new CaseInsensitiveString(name));
    }

    public static Map<String, Location> getHomes(Player player) {
        if (PLAYER_HOMES.containsKey(player.getUniqueId())) {
            return PLAYER_HOMES.get(player.getUniqueId()).entrySet().stream()
                    .map(e -> Map.entry(e.getKey().string, e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return null;
        }
    }

    public static void setHome(Player player, String name, Location loc) {
        UUID uuid = player.getUniqueId();
        if (!PLAYER_HOMES.containsKey(uuid)) {
            PLAYER_HOMES.put(uuid, new HashMap<>());
        }

        String key = uuid + "/" + name;
        if (loc == null) {
            PLAYER_HOMES.get(uuid).remove(new CaseInsensitiveString(name));
            HOME_LOCATION_DB.deleteSQL(key);
        } else {
            PLAYER_HOMES.get(uuid).put(new CaseInsensitiveString(name), loc);
            HOME_LOCATION_DB.setSQL(key, Util.locationAsMap(loc));
        }
    }

    public static void setMaxHomes(Player player, int homes) {
        PLAYER_HOME_CAPACITIES.put(player.getUniqueId(), homes);
        HOME_CAPACITY_DB.setSQL(player.getUniqueId().toString(), Map.of("amount", homes));
    }

    public static int getMaxHomes(Player player) {
        return PLAYER_HOME_CAPACITIES.getOrDefault(player.getUniqueId(), NautilusManager.INSTANCE.getConfig().getInt("homes.startingAmount"));
    }
}
