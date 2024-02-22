package me.liwk.karhu.util.pair;

public class Pair {
   private Object x;
   private Object y;

   public Object getX() {
      return this.x;
   }

   public Object getY() {
      return this.y;
   }

   public void setX(Object x) {
      this.x = x;
   }

   public void setY(Object y) {
      this.y = y;
   }

   public Pair() {
   }

   public Pair(Object x, Object y) {
      this.x = x;
      this.y = y;
   }
}
