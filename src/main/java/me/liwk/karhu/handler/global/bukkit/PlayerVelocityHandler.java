package me.liwk.karhu.handler.global.bukkit;

import java.util.Iterator;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

public class PlayerVelocityHandler implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerVelocity(PlayerVelocityEvent event) {
      if (!event.isCancelled()) {
         KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());
         if (data != null) {
            data.sendTransaction();
            data.setBrokenVelocityVerify(!data.hasSentTickFirst);
            data.setPlayerVelocityCalled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onExplode(EntityExplodeEvent event) {
      if (!event.isCancelled()) {
         Iterator var2 = event.getEntity().getNearbyEntities(4.0, 4.0, 4.0).iterator();

         while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            if (entity instanceof Player && !entity.hasMetadata("NPC")) {
               KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(((Player)entity).getUniqueId());
               if (data != null) {
                  data.sendTransaction();
                  data.setPlayerExplodeCalled(true);
                  data.setBrokenVelocityVerify(true);
                  data.setPlayerVelocityCalled(true);
               }
            }
         }
      }

   }
}
