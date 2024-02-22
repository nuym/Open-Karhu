package me.liwk.karhu.world.chunk;

import java.util.Map;
import me.liwk.karhu.util.gui.Callback;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public interface IChunkManager {
   void getChunk(Location var1, Callback var2);

   Block getChunkBlockAt(Location var1);

   void onChunkUnload(Chunk var1);

   void onChunkLoad(Chunk var1);

   void addWorld(World var1);

   void removeWorld(World var1);

   boolean isChunkLoaded(Location var1);

   void unloadAll();

   int getCacheSize(World var1);

   Map getLoadedChunks();
}
