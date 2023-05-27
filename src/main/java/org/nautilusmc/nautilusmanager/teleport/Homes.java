package org.nautilusmc.nautilusmanager.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.sql.SQLHandler;
import org.nautilusmc.nautilusmanager.util.CaseInsensitiveString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Homes {

    public static final int MAX_NAME_LEN = 16;

    private static final Map<UUID, Map<CaseInsensitiveString, Location>> playerHomes = new HashMap<>();
    private static final Map<UUID, Integer> playerHomeAmts = new HashMap<>();

    public static SQLHandler HOME_LOC_SQL;
    public static SQLHandler HOME_AMT_SQL;

    public static void init() {
        HOME_LOC_SQL = new SQLHandler("homes", "uuid_name") {
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

                    playerHomes.get(uuid).put(new CaseInsensitiveString(uuidName[1]), loc);
                }
            }
        };

        HOME_AMT_SQL = new SQLHandler("home_amounts", "uuid") {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                while (results.next()) {
                    playerHomeAmts.put(UUID.fromString(results.getString("uuid")), results.getInt("amount"));
                }
            }
        };
    }

    public static Location getHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        return !playerHomes.containsKey(uuid) ? null : playerHomes.get(uuid).get(new CaseInsensitiveString(name));
    }

    public static Map<String, Location> getHomes(Player player) {
        return playerHomes.containsKey(player.getUniqueId()) ? playerHomes.get(player.getUniqueId()).entrySet().stream().map(e->Map.entry(e.getKey().string, e.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null;
    }

    public static void setHome(Player player, String name, Location loc) {
        UUID uuid = player.getUniqueId();
        if (!playerHomes.containsKey(player.getUniqueId())) {
            playerHomes.put(uuid, new HashMap<>());
        }

        String key = uuid + "/" + name;
        if (loc == null) {
            playerHomes.get(uuid).remove(new CaseInsensitiveString(name));
            HOME_LOC_SQL.deleteSQL(key);
        } else {
            playerHomes.get(uuid).put(new CaseInsensitiveString(name), loc);
            HOME_LOC_SQL.setSQL(key, locationToMap(loc));
        }
    }

    public static void setMaxHomes(Player player, int homes) {
        playerHomeAmts.put(player.getUniqueId(), homes);
        HOME_AMT_SQL.setSQL(player.getUniqueId().toString(), Map.of("amount", homes));
    }

    public static int getMaxHomes(Player player) {
        return playerHomeAmts.getOrDefault(player.getUniqueId(), NautilusManager.INSTANCE.getConfig().getInt("homes.startingAmount"));
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
