package me.liwk.karhu.world.packet;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.player.BlockUtil;

public class KarhuWorld {
   public final KarhuPlayer data;
   public final Map chunks;

   public KarhuWorld(KarhuPlayer data) {
      this.data = data;
      this.chunks = new Long2ObjectOpenHashMap(81, 0.5F);
   }

   public void updateBlock(int x, int y, int z, int blockID) {
      this.getChunk(x >> 4, z >> 4);
   }

   private void getChunk(int i, int i1) {
      //TODO:
   }


   /*
   public WrappedChunk getChunk(int chunkX, int chunkZ) {
      long chunkPosition = BlockUtil.getChunkPair(chunkX, chunkZ);
      return (WrappedChunk)this.chunks.get(chunkPosition);
   }

    */
}
