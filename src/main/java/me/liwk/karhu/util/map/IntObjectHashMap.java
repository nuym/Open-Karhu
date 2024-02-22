package me.liwk.karhu.util.map;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IntObjectHashMap implements IntObjectMap {
   public static final int DEFAULT_CAPACITY = 8;
   public static final float DEFAULT_LOAD_FACTOR = 0.5F;
   private static final Object NULL_VALUE = new Object();
   private int maxSize;
   private final float loadFactor;
   private int[] keys;
   private Object[] values;
   private int size;
   private int mask;
   private final Set keySet;
   private final Set entrySet;
   private final Iterable entries;

   public IntObjectHashMap() {
      this(8, 0.5F);
   }

   public IntObjectHashMap(int initialCapacity) {
      this(initialCapacity, 0.5F);
   }

   public IntObjectHashMap(int initialCapacity, float loadFactor) {
      this.keySet = new KeySet();
      this.entrySet = new EntrySet();
      this.entries = new Iterable() {
         public Iterator iterator() {
            IntObjectHashMap var10002 = IntObjectHashMap.this;
            var10002.getClass();
            return new PrimitiveIterator(var10002);
         }
      };
      if (!(loadFactor <= 0.0F) && !(loadFactor > 1.0F)) {
         this.loadFactor = loadFactor;
         int capacity = MapMathUtil.safeFindNextPositivePowerOfTwo(initialCapacity);
         this.mask = capacity - 1;
         this.keys = new int[capacity];
         Object[] temp = (Object[])(new Object[capacity]);
         this.values = temp;
         this.maxSize = this.calcMaxSize(capacity);
      } else {
         throw new IllegalArgumentException("loadFactor must be > 0 and <= 1");
      }
   }

   private static Object toExternal(Object value) {
      assert value != null : "null is not a legitimate internal value. Concurrent Modification?";

      return value == NULL_VALUE ? null : value;
   }

   private static Object toInternal(Object value) {
      return value == null ? NULL_VALUE : value;
   }

   public Object get(int key) {
      int index = this.indexOf(key);
      return index == -1 ? null : toExternal(this.values[index]);
   }

   public Object put(int key, Object value) {
      int startIndex = this.hashIndex(key);
      int index = startIndex;

      do {
         if (this.values[index] == null) {
            this.keys[index] = key;
            this.values[index] = toInternal(value);
            this.growSize();
            return null;
         }

         if (this.keys[index] == key) {
            Object previousValue = this.values[index];
            this.values[index] = toInternal(value);
            return toExternal(previousValue);
         }
      } while((index = this.probeNext(index)) != startIndex);

      throw new IllegalStateException("Unable to insert");
   }

   public void putAll(Map sourceMap) {
      if (sourceMap instanceof IntObjectHashMap) {
         IntObjectHashMap source = (IntObjectHashMap)sourceMap;

         for(int i = 0; i < source.values.length; ++i) {
            Object sourceValue = source.values[i];
            if (sourceValue != null) {
               this.put(source.keys[i], sourceValue);
            }
         }
      } else {
         Iterator var2 = sourceMap.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry entry = (Map.Entry)var2.next();
            this.put((Integer)entry.getKey(), entry.getValue());
         }
      }

   }

   public Object remove(int key) {
      int index = this.indexOf(key);
      if (index == -1) {
         return null;
      } else {
         Object prev = this.values[index];
         this.removeAt(index);
         return toExternal(prev);
      }
   }

   public int size() {
      return this.size;
   }

   public boolean isEmpty() {
      return this.size == 0;
   }

   public void clear() {
      Arrays.fill(this.keys, 0);
      Arrays.fill(this.values, (Object)null);
      this.size = 0;
   }

   public boolean containsKey(int key) {
      return this.indexOf(key) >= 0;
   }

   public boolean containsValue(Object value) {
      Object v1 = toInternal(value);
      Object[] var3 = this.values;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Object v2 = var3[var5];
         if (v2 != null && v2.equals(v1)) {
            return true;
         }
      }

      return false;
   }

   public Iterable entries() {
      return this.entries;
   }

   public Collection values() {
      return new 2(this);
   }

   public int hashCode() {
      int hash = this.size;
      int[] var2 = this.keys;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int key = var2[var4];
         hash ^= hashCode(key);
      }

      return hash;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof IntObjectMap)) {
         return false;
      } else {
         IntObjectMap other = (IntObjectMap)obj;
         if (this.size != other.size()) {
            return false;
         } else {
            for(int i = 0; i < this.values.length; ++i) {
               Object value = this.values[i];
               if (value != null) {
                  int key = this.keys[i];
                  Object otherValue = other.get(key);
                  if (value == NULL_VALUE) {
                     if (otherValue != null) {
                        return false;
                     }
                  } else if (!value.equals(otherValue)) {
                     return false;
                  }
               }
            }

            return true;
         }
      }
   }

   public boolean containsKey(Object key) {
      return this.containsKey(this.objectToKey(key));
   }

   public Object get(Object key) {
      return this.get(this.objectToKey(key));
   }

   public Object put(Integer key, Object value) {
      return this.put(this.objectToKey(key), value);
   }

   public Object remove(Object key) {
      return this.remove(this.objectToKey(key));
   }

   public Set keySet() {
      return this.keySet;
   }

   public Set entrySet() {
      return this.entrySet;
   }

   private int objectToKey(Object key) {
      return (Integer)key;
   }

   private int indexOf(int key) {
      int startIndex = this.hashIndex(key);
      int index = startIndex;

      do {
         if (this.values[index] == null) {
            return -1;
         }

         if (key == this.keys[index]) {
            return index;
         }
      } while((index = this.probeNext(index)) != startIndex);

      return -1;
   }

   private int hashIndex(int key) {
      return hashCode(key) & this.mask;
   }

   private static int hashCode(int key) {
      return key;
   }

   private int probeNext(int index) {
      return index + 1 & this.mask;
   }

   private void growSize() {
      ++this.size;
      if (this.size > this.maxSize) {
         if (this.keys.length == Integer.MAX_VALUE) {
            throw new IllegalStateException("Max capacity reached at size=" + this.size);
         }

         this.rehash(this.keys.length << 1);
      }

   }

   private boolean removeAt(int index) {
      --this.size;
      this.keys[index] = 0;
      this.values[index] = null;
      int nextFree = index;
      int i = this.probeNext(index);

      for(Object value = this.values[i]; value != null; value = this.values[i = this.probeNext(i)]) {
         int key = this.keys[i];
         int bucket = this.hashIndex(key);
         if (i < bucket && (bucket <= nextFree || nextFree <= i) || bucket <= nextFree && nextFree <= i) {
            this.keys[nextFree] = key;
            this.values[nextFree] = value;
            this.keys[i] = 0;
            this.values[i] = null;
            nextFree = i;
         }
      }

      return nextFree != index;
   }

   private int calcMaxSize(int capacity) {
      int upperBound = capacity - 1;
      return Math.min(upperBound, (int)((float)capacity * this.loadFactor));
   }

   private void rehash(int newCapacity) {
      int[] oldKeys = this.keys;
      Object[] oldVals = this.values;
      this.keys = new int[newCapacity];
      Object[] temp = (Object[])(new Object[newCapacity]);
      this.values = temp;
      this.maxSize = this.calcMaxSize(newCapacity);
      this.mask = newCapacity - 1;

      for(int i = 0; i < oldVals.length; ++i) {
         Object oldVal = oldVals[i];
         if (oldVal != null) {
            int oldKey = oldKeys[i];

            int index;
            for(index = this.hashIndex(oldKey); this.values[index] != null; index = this.probeNext(index)) {
            }

            this.keys[index] = oldKey;
            this.values[index] = oldVal;
         }
      }

   }

   public String toString() {
      if (this.isEmpty()) {
         return "{}";
      } else {
         StringBuilder sb = new StringBuilder(4 * this.size);
         sb.append('{');
         boolean first = true;

         for(int i = 0; i < this.values.length; ++i) {
            Object value = this.values[i];
            if (value != null) {
               if (!first) {
                  sb.append(", ");
               }

               sb.append(this.keyToString(this.keys[i])).append('=').append(value == this ? "(this Map)" : toExternal(value));
               first = false;
            }
         }

         return sb.append('}').toString();
      }
   }

   protected String keyToString(int key) {
      return Integer.toString(key);
   }

   // $FF: synthetic method
   static int access$300(IntObjectHashMap x0) {
      return x0.size;
   }

   // $FF: synthetic method
   static int[] access$400(IntObjectHashMap x0) {
      return x0.keys;
   }

   // $FF: synthetic method
   static Object[] access$500(IntObjectHashMap x0) {
      return x0.values;
   }

   // $FF: synthetic method
   static Object access$600(Object x0) {
      return toExternal(x0);
   }

   // $FF: synthetic method
   static Object access$700(Object x0) {
      return toInternal(x0);
   }

   // $FF: synthetic method
   static boolean access$900(IntObjectHashMap x0, int x1) {
      return x0.removeAt(x1);
   }

   // $FF: synthetic method
   static Set access$1000(IntObjectHashMap x0) {
      return x0.entrySet;
   }

   private final class EntrySet extends AbstractSet {
      private EntrySet() {
      }

      public Iterator iterator() {
         IntObjectHashMap var10002 = IntObjectHashMap.this;
         var10002.getClass();
         return new MapIterator(var10002);
      }

      public int size() {
         return IntObjectHashMap.this.size();
      }

      // $FF: synthetic method
      EntrySet(Object x1) {
         this();
      }
   }

   private final class KeySet extends AbstractSet {
      private KeySet() {
      }

      public int size() {
         return IntObjectHashMap.this.size();
      }

      public boolean contains(Object o) {
         return IntObjectHashMap.this.containsKey(o);
      }

      public boolean remove(Object o) {
         return IntObjectHashMap.this.remove(o) != null;
      }

      public boolean retainAll(Collection retainedKeys) {
         boolean changed = false;
         Iterator iter = IntObjectHashMap.this.entries().iterator();

         while(iter.hasNext()) {
            IntObjectMap.PrimitiveEntry entry = (IntObjectMap.PrimitiveEntry)iter.next();
            if (!retainedKeys.contains(entry.key())) {
               changed = true;
               iter.remove();
            }
         }

         return changed;
      }

      public void clear() {
         IntObjectHashMap.this.clear();
      }

      public Iterator iterator() {
         return new 1(this);
      }

      // $FF: synthetic method
      KeySet(Object x1) {
         this();
      }
   }
}
