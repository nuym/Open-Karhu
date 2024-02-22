package me.liwk.karhu.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class KarhuStream {
   private Collection c;

   public KarhuStream(Collection l) {
      this.c = l;
   }

   public boolean any(Predicate p) {
      Iterator var2 = this.c.iterator();

      Object t;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         t = var2.next();
      } while(!p.test(t));

      return true;
   }

   public boolean all(Predicate p) {
      Iterator var2 = this.c.iterator();

      Object t;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         t = var2.next();
      } while(p.test(t));

      return false;
   }

   public Object find(Predicate p) {
      Iterator var2 = this.c.iterator();

      Object t;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         t = var2.next();
      } while(!p.test(t));

      return t;
   }

   public void setCollection(Collection c) {
      this.c = c;
   }

   public boolean isEmpty() {
      return this.c.isEmpty();
   }

   public void forEach(Consumer consumer) {
      this.c.forEach(consumer);
   }
}
