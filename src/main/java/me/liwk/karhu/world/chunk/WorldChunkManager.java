package me.liwk.karhu.world.chunk;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.util.Conditions;
import me.liwk.karhu.util.gui.Callback;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class WorldChunkManager implements IChunkManager {
   private final Map loadedChunks = new HashMap();
   private long lastAskTick;

   public void getChunk(Location location, Callback chunkCallback) {
      synchronized(this.loadedChunks) {
         World world = location.getWorld();
         Conditions.notNull(world, "location world cannot be null");
         Long2ObjectMap chunkMap = (Long2ObjectMap)this.loadedChunks.computeIfAbsent(world, (k) -> {
            return new Long2ObjectOpenHashMap();
         });
         if (chunkMap.isEmpty()) {
            this.somethingTriedDoingSomethingStupidErrorMessage(world);
         } else {
            Chunk chunk = (Chunk)chunkMap.get(BlockUtil.getChunkPair(location));
            if (chunk != null) {
               chunkCallback.call(chunk);
            }
         }

      }
   }

   public Block getChunkBlockAt(Location location) {
      synchronized(this.loadedChunks) {
         World world = location.getWorld();
         Conditions.notNull(world, "location world cannot be null");
         Long2ObjectMap chunkMap = (Long2ObjectMap)this.loadedChunks.computeIfAbsent(world, (k) -> {
            return new Long2ObjectOpenHashMap();
         });
         if (chunkMap.isEmpty()) {
            return null;
         } else {
            Chunk chunk = (Chunk)chunkMap.get(BlockUtil.getChunkPair(location));
            if (chunk == null) {
               if (Karhu.getInstance().getServerTick() - this.lastAskTick >= 1L) {
                  Tasker.run(() -> {
                     Chunk[] var2 = world.getLoadedChunks();
                     int var3 = var2.length;

                     for(int var4 = 0; var4 < var3; ++var4) {
                        Chunk c = var2[var4];
                        this.onChunkLoad(c);
                     }

                  });
               }

               this.lastAskTick = Karhu.getInstance().getServerTick();
               return null;
            } else {
               int blockY = location.getBlockY();
               boolean invalidCoord = blockY > world.getMaxHeight() || blockY < 0;
               return Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_13) && invalidCoord ? location.getBlock() : chunk.getBlock(location.getBlockX() & 15, blockY, location.getBlockZ() & 15);
            }
         }
      }
   }

   public void onChunkLoad(Chunk chunk) {
      synchronized(this.loadedChunks) {
         ((Long2ObjectMap)this.loadedChunks.computeIfAbsent(chunk.getWorld(), (k) -> {
            return new Long2ObjectOpenHashMap();
         })).put(BlockUtil.getChunkPair(chunk), chunk);
      }
   }

   public void onChunkUnload(Chunk chunk) {
      synchronized(this.loadedChunks) {
         Map chunkMap = (Map)this.loadedChunks.get(chunk.getWorld());
         if (chunkMap != null) {
            chunkMap.remove(BlockUtil.getChunkPair(chunk));
         }

      }
   }

   public boolean isChunkLoaded(Location l) {
      synchronized(this.loadedChunks) {
         World world = l.getWorld();
         Conditions.notNull(world, "location world cannot be null");
         Long2ObjectMap chunkMap = (Long2ObjectMap)this.loadedChunks.get(world);
         boolean invalid = chunkMap == null;
         boolean empty = !invalid && chunkMap.isEmpty();
         if (!invalid && !empty) {
            Chunk chunk = (Chunk)chunkMap.get(BlockUtil.getChunkPair(l));
            return chunk != null && chunk.isLoaded();
         } else {
            return world.isChunkLoaded(l.getBlockX() >> 4, l.getBlockZ() >> 4);
         }
      }
   }

   public void addWorld(World world) {
      synchronized(this.loadedChunks) {
         this.loadedChunks.computeIfAbsent(world, (k) -> {
            return new Long2ObjectOpenHashMap();
         });
      }
   }

   public void removeWorld(World world) {
      synchronized(this.loadedChunks) {
         this.loadedChunks.remove(world);
      }
   }

   public void unloadAll() {
      synchronized(this.loadedChunks) {
         this.loadedChunks.clear();
      }
   }

   public int getCacheSize(World world) {
      synchronized(this.loadedChunks) {
         return ((Long2ObjectMap)this.loadedChunks.get(world)).size();
      }
   }

   public Map getLoadedChunks() {
      return this.loadedChunks;
   }

   private void somethingTriedDoingSomethingStupidErrorMessage(World world) {
      if (world == null) {
         Bukkit.getLogger().log(Level.SEVERE, "Karhu attempted to access a chunk in a non-existent world, this should never happen null");
      } else {
         Bukkit.getLogger().log(Level.SEVERE, "Karhu attempted to access a chunk in a non-existent world, this should never happen " + world.getName());
      }

   }
}
