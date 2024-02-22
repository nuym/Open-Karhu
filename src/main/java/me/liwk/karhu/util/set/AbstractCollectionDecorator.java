package me.liwk.karhu.util.set;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public abstract class AbstractCollectionDecorator implements Collection, Serializable {
   private static final long serialVersionUID = 6249888059822088500L;
   private Collection collection;

   protected AbstractCollectionDecorator() {
   }

   protected AbstractCollectionDecorator(Collection coll) {
      if (coll == null) {
         throw new NullPointerException("Collection must not be null.");
      } else {
         this.collection = coll;
      }
   }

   protected Collection decorated() {
      return this.collection;
   }

   protected void setCollection(Collection coll) {
      this.collection = coll;
   }

   public boolean add(Object object) {
      return this.decorated().add(object);
   }

   public boolean addAll(Collection coll) {
      return this.decorated().addAll(coll);
   }

   public void clear() {
      this.decorated().clear();
   }

   public boolean contains(Object object) {
      return this.decorated().contains(object);
   }

   public boolean isEmpty() {
      return this.decorated().isEmpty();
   }

   public Iterator iterator() {
      return this.decorated().iterator();
   }

   public boolean remove(Object object) {
      return this.decorated().remove(object);
   }

   public int size() {
      return this.decorated().size();
   }

   public Object[] toArray() {
      return this.decorated().toArray();
   }

   public Object[] toArray(Object[] object) {
      return this.decorated().toArray(object);
   }

   public boolean containsAll(Collection coll) {
      return this.decorated().containsAll(coll);
   }

   public boolean removeIf(Predicate filter) {
      return this.decorated().removeIf(filter);
   }

   public boolean removeAll(Collection coll) {
      return this.decorated().removeAll(coll);
   }

   public boolean retainAll(Collection coll) {
      return this.decorated().retainAll(coll);
   }

   public String toString() {
      return this.decorated().toString();
   }
}
