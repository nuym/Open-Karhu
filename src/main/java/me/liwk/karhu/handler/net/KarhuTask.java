package me.liwk.karhu.handler.net;

import me.liwk.karhu.util.gui.Callback;

public class KarhuTask {
   private int id;
   private final Callback callback;

   public KarhuTask(Callback callback) {
      this.callback = callback;
   }

   public KarhuTask(Callback callback, int id) {
      this.callback = callback;
      this.id = id;
   }

   public void runTask() {
      this.callback.call(this.id);
   }

   public int getId() {
      return this.id;
   }

   public Callback getCallback() {
      return this.callback;
   }

   public void setId(int id) {
      this.id = id;
   }
}
