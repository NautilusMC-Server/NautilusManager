package org.nautilusmc.nautilusmanager.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class SQLSyncedPerPlayerList<V, T> extends HashMap<UUID, List<V>> {
    private final Class<T> serializedType;
    private final Function<T, V> valueParser;
    private final Function<V, T> valueSerializer;

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
                clear();

                while (results.next()) {
                    UUID player = decodePlayer(results.getString("uuid"));
                    V value = valueParser.apply(results.getObject(valueColumn, serializedType));

                    putIfAbsent(player, new ArrayList<>());
                    get(player).add(value);
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
        putIfAbsent(player, new ArrayList<>());
        List<V> list = get(player);

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
        if (!containsKey(player)) return false;

        List<V> list = get(player);

        if (!list.contains(value)) return false;

        database.deleteEntry(encodeIdentifier(player, list.indexOf(value)));
        list.remove(value);

        return true;
    }

    public boolean contains(UUID player, V value) {
        return containsKey(player) && get(player).contains(value);
    }
}
