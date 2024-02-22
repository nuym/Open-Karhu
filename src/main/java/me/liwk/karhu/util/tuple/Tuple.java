package me.liwk.karhu.util.tuple;

public class Tuple {
   private Object a;
   private Object b;

   public Tuple(Object var1, Object var2) {
      this.a = var1;
      this.b = var2;
   }

   public Object a() {
      return this.a;
   }

   public Object b() {
      return this.b;
   }
}
