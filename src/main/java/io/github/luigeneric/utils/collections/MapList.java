package io.github.luigeneric.utils.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapList<K, V>
{
    private final Map<K, List<V>> internalMap;


    private MapList(final Map<K, List<V>> internalMap)
    {
        this.internalMap = internalMap;
    }

    public static <K, V> MapList<K, V> create(final boolean concurrent)
    {
        if (concurrent)
        {
            return new MapList<>(new ConcurrentHashMap<>());
        }
        else
        {
            return new MapList<>(new HashMap<>());
        }
    }

    /**
     * Creates a MapList of Type Key K and List<V> in non concurrent
     * @return
     * @param <K>
     * @param <V>
     */
    public static <K, V> MapList<K, V> create()
    {
        return create(false);
    }

    public void put(final K k, final V v)
    {
        List<V> existingLst;
        if (this.internalMap.containsKey(k))
            existingLst = this.internalMap.get(k);
        else
            existingLst = new ArrayList<>();
        existingLst.add(v);
        this.internalMap.put(k, existingLst);
    }

    public Optional<List<V>> get(final K k)
    {
        final List<V> existing = this.internalMap.get(k);
        return Optional.ofNullable(existing);
    }
    public Optional<V> getFirst(final K k)
    {
        final List<V> tmpLst = this.internalMap.get(k);
        if (tmpLst == null) return Optional.empty();
        if (tmpLst.isEmpty()) return Optional.empty();
        return Optional.ofNullable(tmpLst.get(0));
    }
}
