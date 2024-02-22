package me.liwk.karhu.util.evictinglist;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class ConcurrentEvictingList extends ConcurrentLinkedDeque {
   private int maxSize;

   public ConcurrentEvictingList(int maxSize) {
      this.maxSize = maxSize;
   }

   public ConcurrentEvictingList(Collection c, int maxSize) {
      super(c);
      this.maxSize = maxSize;
   }

   public int getMaxSize() {
      return this.maxSize;
   }

   public boolean add(Object t) {
      if (this.size() >= this.maxSize) {
         this.remove(0);
      }

      return super.add(t);
   }

   public boolean addAll(Collection c) {
      return c.stream().anyMatch(this::add);
   }

   public Stream stream() {
      return (new CopyOnWriteArrayList(this)).stream();
   }
}
