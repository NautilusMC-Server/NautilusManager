package org.nautilusmc.nautilusmanager.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.util.CaseInsensitiveString;
import org.nautilusmc.nautilusmanager.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Warps {
    public static final int MAX_NAME_LENGTH = 16;

    private static final Map<CaseInsensitiveString, Location> WARPS = new HashMap<>();

    public static SQLHandler WARP_LOCATION_DB;

    public static void init() {
        WARP_LOCATION_DB = new SQLHandler("warps", "name") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                while (results.next()) {
                    WARPS.put(new CaseInsensitiveString(results.getString("name")), new Location(
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
        return WARPS.keySet().stream().map(s -> s.string).toList();
    }

    public static Location getWarp(String name) {
        return WARPS.get(new CaseInsensitiveString(name));
    }

    public static void createWarp(String name, Location location) {
        WARPS.put(new CaseInsensitiveString(name), location);
        WARP_LOCATION_DB.setSQL(name, Util.locationAsMap(location));
    }

    public static void removeWarp(String name) {
        WARPS.remove(new CaseInsensitiveString(name));
        WARP_LOCATION_DB.deleteSQL(name);
    }
}
