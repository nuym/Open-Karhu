package me.liwk.karhu.util.map;

import java.util.Map;
import me.liwk.karhu.util.map.IntObjectMap.PrimitiveEntry;

public interface IntObjectMap<V> extends Map<Integer, V> {
   V get(int var1);

   V put(int var1, V var2);

   V remove(int var1);

   Iterable<PrimitiveEntry<V>> entries();

   boolean containsKey(int var1);
}
