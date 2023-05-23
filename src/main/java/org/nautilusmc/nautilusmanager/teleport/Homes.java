package org.nautilusmc.nautilusmanager.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Homes {

    public static final int MAX_NAME_LEN = 16;

    private static final Map<UUID, Map<String, Location>> playerHomes = new HashMap<>();

    public static SQLHandler SQL_HANDLER;

    public static void init() {
        SQL_HANDLER = new SQLHandler("homes", "uuid_name") {
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

                    if (!playerHomes.containsKey(uuid)) playerHomes.put(uuid, new HashMap<>());

                    playerHomes.get(uuid).put(uuidName[1], loc);
                }
            }
        };
    }

    public static Location getHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        return !playerHomes.containsKey(uuid) ? null : playerHomes.get(uuid).get(name);
    }

    public static Map<String, Location> getHomes(Player player) {
        return playerHomes.get(player.getUniqueId());
    }

    public static void setHome(Player player, String name, Location loc) {
        UUID uuid = player.getUniqueId();
        if (!playerHomes.containsKey(player.getUniqueId())) {
            playerHomes.put(uuid, new HashMap<>());
        }

        String key = uuid + "/" + name;
        if (loc == null) {
            playerHomes.get(uuid).remove(name);
            SQL_HANDLER.deleteSQL(key);
        } else {
            playerHomes.get(uuid).put(name, loc);
            SQL_HANDLER.setSQL(key, locationToMap(loc));
        }
    }

    private static Map<String, Object> locationToMap(Location loc) {
        return Map.of(
                "world", loc.getWorld().getUID().toString(),
                "x", loc.getX(),
                "y", loc.getY(),
                "z", loc.getZ(),
                "pitch", loc.getPitch(),
                "yaw", loc.getYaw());
    }
}
