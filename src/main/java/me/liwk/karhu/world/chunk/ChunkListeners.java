package me.liwk.karhu.world.chunk;

import me.liwk.karhu.Karhu;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public final class ChunkListeners implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChunkLoad(ChunkLoadEvent e) {
      Karhu.getInstance().getChunkManager().onChunkLoad(e.getChunk());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChunkUnload(ChunkUnloadEvent e) {
      Karhu.getInstance().getChunkManager().onChunkUnload(e.getChunk());
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      Karhu.getInstance().getChunkManager().addWorld(e.getWorld());
      Chunk[] var2 = e.getWorld().getLoadedChunks();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Chunk chunk = var2[var4];
         Karhu.getInstance().getChunkManager().onChunkLoad(chunk);
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldUnload(WorldUnloadEvent e) {
      if (!e.isCancelled()) {
         Karhu.getInstance().getChunkManager().removeWorld(e.getWorld());
      }
   }
}
