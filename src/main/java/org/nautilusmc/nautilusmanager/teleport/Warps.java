package org.nautilusmc.nautilusmanager.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.util.CaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Warps {

    public static final int MAX_NAME_LEN = 16;

    private static final Map<String, Location> warps = new CaseInsensitiveMap<>();

    public static SQLHandler SQL_HANDLER;

    public static void init() {
        SQL_HANDLER = new SQLHandler("warps", "name") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                while (results.next()) {
                    warps.put(results.getString("name"), new Location(
                            Bukkit.getWorld(UUID.fromString(results.getString("world"))),
                            results.getDouble("x"),
                            results.getDouble("y"),
                            results.getDouble("z"),
                            results.getFloat("yaw"),
                            results.getFloat("pitch")
                    ));
                }
            }
        };
    }

    public static Collection<String> getWarps() {
        return warps.keySet();
    }

    public static Location getWarp(String name) {
        return warps.get(name);
    }

    public static void newWarp(String name, Location location) {
        warps.put(name, location);
        SQL_HANDLER.setSQL(name, locationToMap(location));
    }

    public static void removeWarp(String name) {
        warps.remove(name);
        SQL_HANDLER.deleteSQL(name);
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
