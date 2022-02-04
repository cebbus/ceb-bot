package com.cebbus.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LimitedHashMap<K, V> extends LinkedHashMap<K, V> {

    private int maxEntries;

    private LimitedHashMap() {
    }

    public static <K, V> LimitedHashMap<K, V> create(int limit) {
        LimitedHashMap<K, V> map = new LimitedHashMap<>();
        map.maxEntries = limit;

        return map;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LimitedHashMap<?, ?> that = (LimitedHashMap<?, ?>) o;
        return maxEntries == that.maxEntries;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxEntries);
    }
}
