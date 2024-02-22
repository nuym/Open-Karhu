package me.liwk.karhu.util.set;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class PassiveExpiringSet extends AbstractSetDecorator implements Serializable {
   private static final long serialVersionUID = 1L;
   private final long timeToLiveMillis;
   private final Map expirationMap = new HashMap();

   public PassiveExpiringSet(long timeToLiveMillis) {
      super(new HashSet());
      this.timeToLiveMillis = timeToLiveMillis;
   }

   public boolean add(Object value) {
      this.removeAllExpired();
      this.expirationMap.put(value, this.now());
      return super.add(value);
   }

   public boolean remove(Object key) {
      this.expirationMap.remove(key);
      return super.remove(key);
   }

   public boolean contains(Object value) {
      this.removeAllExpired();
      return super.contains(value);
   }

   public int size() {
      this.removeAllExpired();
      return super.size();
   }

   public void clear() {
      this.expirationMap.clear();
      super.clear();
   }

   private void removeAllExpired() {
      Iterator iterator = this.expirationMap.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry expirationEntry = (Map.Entry)iterator.next();
         if (this.expired((Long)expirationEntry.getValue())) {
            super.remove(expirationEntry.getKey());
            iterator.remove();
         }
      }

   }

   private boolean expired(long insertTime) {
      return this.now() - insertTime >= this.timeToLiveMillis;
   }

   private long now() {
      return System.currentTimeMillis();
   }
}
