package me.liwk.karhu.handler.interfaces;

import me.liwk.karhu.util.location.CustomLocation;

public interface ICrashHandler {
   void handleClientKeepAlive();

   void handleFlying(boolean var1, boolean var2, CustomLocation var3, CustomLocation var4);

   void handleArm();

   void handleWindowClick(int var1, int var2, int var3, int var4);

   void handleSlot();

   void handleCustomPayload();

   void handlePlace();
}
