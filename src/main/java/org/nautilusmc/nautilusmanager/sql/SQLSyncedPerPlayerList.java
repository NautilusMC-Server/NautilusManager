package org.nautilusmc.nautilusmanager.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class SQLSyncedPerPlayerList<V, T> {

    private final Class<T> serializedType;
    private final Function<T, V> valueParser;
    private final Function<V, T> valueSerializer;
    private final HashMap<UUID, List<V>> map = new HashMap<>();

    private final String valueColumn;
    private final int maxSize;

    private SQLHandler database;

    public SQLSyncedPerPlayerList(Class<T> serializedType, Function<V, T> valueSerializer, Function<T, V> valueParser, String valueColumn, int maxSize) {
        this.serializedType = serializedType;
        this.valueSerializer = valueSerializer;
        this.valueParser = valueParser;

        this.valueColumn = valueColumn;
        this.maxSize = maxSize;
    }

    public void initSQL(String table) {
        database = new SQLHandler(table) {
            @Override
            public void update(ResultSet results) throws SQLException {
                map.clear();

                while (results.next()) {
                    UUID player = decodePlayer(results.getString("uuid"));
                    V value = valueParser.apply(results.getObject(valueColumn, serializedType));

                    map.putIfAbsent(player, new ArrayList<>());
                    map.get(player).add(value);
                }
            }
        };
    }

    private static String encodeIdentifier(UUID player, int index) {
        return player + "_" + index;
    }

    private static UUID decodePlayer(String identifier) {
        return UUID.fromString(identifier.substring(0, identifier.indexOf('_')));
    }

    private static int decodeIndex(String identifier) {
        return Integer.parseInt(identifier.substring(identifier.indexOf('_') + 1));
    }

    public boolean add(UUID player, V value) {
        map.putIfAbsent(player, new ArrayList<>());
        List<V> list = map.get(player);

        int index = list.indexOf(null);
        if (index == -1) {
            index = list.size();
            if (index > maxSize) return false;

            list.add(value);
        } else {
            list.set(index, value);
        }
        database.setValues(encodeIdentifier(player, index), Map.of(valueColumn, valueSerializer.apply(value)));
        return true;
    }

    public boolean remove(UUID player, V value) {
        if (!map.containsKey(player)) return false;

        List<V> list = map.get(player);

        if (!list.contains(value)) return false;

        database.deleteEntry(encodeIdentifier(player, list.indexOf(value)));
        list.remove(value);

        return true;
    }

    public boolean remove(UUID player) {
        if (!map.containsKey(player)) return false;

        List<V> list = map.get(player);

        for (int i = 0; i < list.size(); i++) {
            database.deleteEntry(encodeIdentifier(player, i));
        }

        map.remove(player);
        return true;
    }

    public boolean contains(UUID player, V value) {
        return map.containsKey(player) && map.get(player).contains(value);
    }

    public List<V> get(UUID player) {
        return map.containsKey(player) ? new ArrayList<>(map.get(player)) : null;
    }

    public List<V> getOrDefault(UUID player, List<V> def) {
        return new ArrayList<>(map.getOrDefault(player, def));
    }
}
