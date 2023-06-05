package org.nautilusmc.nautilusmanager.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class SQLSyncedPerPlayerList<V, T> extends HashMap<UUID, List<V>> {

    private final Class<T> serializedType;
    private final Function<T, V> valueParser;
    private final Function<V, T> valueSerializer;

    String valueColumn;
    private final int maxSize;

    private SQLHandler sql;

    public SQLSyncedPerPlayerList(Class<T> serializedType, Function<V, T> valueSerializer, Function<T, V> valueParser, String valueColumn, int maxSize) {
        this.serializedType = serializedType;
        this.valueSerializer = valueSerializer;
        this.valueParser = valueParser;

        this.valueColumn = valueColumn;
        this.maxSize = maxSize;
    }

    public void initSQL(String table) {
        sql = new SQLHandler(table) {
            @Override
            public void updateSQL(ResultSet results) throws SQLException {
                clear();

                while (results.next()) {
                    String uuidString = results.getString("uuid");

                    UUID uuid = UUID.fromString(uuidString.split("_")[0]);
                    V value = valueParser.apply(results.getObject(valueColumn, serializedType));

                    putIfAbsent(uuid, new ArrayList<>());
                    get(uuid).add(value);
                }
            }
        };
    }

    public boolean add(UUID player, V value) {
        putIfAbsent(player, new ArrayList<>());
        List<V> list = get(player);

        int idx = list.indexOf(null);
        if (idx == -1) {
            idx = list.size();
            if (idx > maxSize) return false;

            list.add(value);
        } else {
            list.set(idx, value);
        }
        sql.setSQL(player+"_"+idx, Map.of(valueColumn, valueSerializer.apply(value)));
        return true;
    }

    public boolean remove(UUID player, V value) {
        if (!containsKey(player)) return false;

        List<V> list = get(player);

        if (!list.contains(value)) return false;

        sql.deleteSQL(player+"_"+list.indexOf(value));
        list.remove(value);

        return true;
    }

    public boolean contains(UUID player, V value) {
        return containsKey(player) && get(player).contains(value);
    }
}
