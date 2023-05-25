package org.nautilusmc.nautilusmanager.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CaseInsensitiveMap<V> extends HashMap<String, V> {
    
    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && super.keySet().stream().anyMatch(k -> k.equalsIgnoreCase((String) key));
    }

    @Override
    public V get(Object key) {
        return super.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase((String) key)).findFirst().map(Entry::getValue).orElse(null);
    }

    @Nullable
    @Override
    public V put(String key, V value) {
        super.keySet().stream().filter(k -> k.equalsIgnoreCase(key)).findFirst().ifPresent(super::remove);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        if (!(key instanceof String s)) return null;

        Object[] result = new Object[1];
        super.keySet().stream().filter(k -> k.equalsIgnoreCase(s)).findFirst().ifPresent(k->result[0] = super.remove(k));

        return (V) result[0];
    }
}
