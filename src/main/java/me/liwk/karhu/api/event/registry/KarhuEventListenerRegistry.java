package me.liwk.karhu.api.event.registry;

import me.liwk.karhu.api.event.KarhuEvent;
import me.liwk.karhu.api.event.KarhuListener;

public interface KarhuEventListenerRegistry {
   boolean fireEvent(KarhuEvent var1);

   void shutdown();

   void addListener(KarhuListener var1);

   void removeListener(KarhuListener var1);
}
