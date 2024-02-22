package me.liwk.karhu.manager;

import com.github.retrooper.packetevents.protocol.player.User;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.entity.Player;

public final class PlayerDataManager {
   private final Map playerDataMap = new ConcurrentHashMap();
   private final Karhu karhu;

   public PlayerDataManager(Karhu karhu) {
      this.karhu = karhu;
   }

   public KarhuPlayer getPlayerData(Player player) {
      return (KarhuPlayer)this.playerDataMap.get(player.getUniqueId());
   }

   public KarhuPlayer getPlayerData(User user) {
      return (KarhuPlayer)this.playerDataMap.get(user.getUUID());
   }

   public KarhuPlayer getPlayerData(UUID uuid) {
      return (KarhuPlayer)this.playerDataMap.get(uuid);
   }

   public KarhuPlayer remove(UUID uuid) {
      KarhuPlayer data = this.getPlayerData(uuid);
      if (data != null) {
         data.setRemovingObject(true);
         Karhu.getInstance().getThreadManager().shutdownThread(data);
      }

      return (KarhuPlayer)this.playerDataMap.remove(uuid);
   }

   public KarhuPlayer add(UUID uuid, long now) {
      return (KarhuPlayer)this.playerDataMap.put(uuid, new KarhuPlayer(uuid, this.karhu, now));
   }

   public Map getPlayerDataMap() {
      return this.playerDataMap;
   }
}
