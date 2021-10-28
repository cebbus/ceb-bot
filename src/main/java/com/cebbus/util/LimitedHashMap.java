package com.cebbus.util;

import java.util.LinkedHashMap;
import java.util.Map;

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
}
