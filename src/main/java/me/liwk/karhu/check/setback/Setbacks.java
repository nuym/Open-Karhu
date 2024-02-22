package me.liwk.karhu.check.setback;

import me.liwk.karhu.check.setback.Setbacks.1;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public final class Setbacks {
   public static Location forgeToRotatedLocation(Location loc, KarhuPlayer data) {
      Location locMod = loc.clone();
      locMod.setYaw(data.getLocation().yaw);
      locMod.setPitch(data.getLocation().pitch);
      return locMod;
   }

   public static CustomLocation forgeToRotatedLocation(CustomLocation loc, KarhuPlayer data) {
      CustomLocation locMod = loc.clone();
      locMod.setYaw(data.getLocation().yaw);
      locMod.setPitch(data.getLocation().pitch);
      return locMod;
   }

   public static Location moveOutOfBlockSafely(double x, double z, KarhuPlayer data) {
      int blockX = MathHelper.floor(x);
      int blockZ = MathHelper.floor(z);
      if (!suffocatesAt(blockX, blockZ, data)) {
         return null;
      } else {
         double relativeXMovement = x - (double)blockX;
         double relativeZMovement = z - (double)blockZ;
         BlockFace direction = null;
         double lowestValue = Double.MAX_VALUE;
         BlockFace[] var14 = new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};
         int var15 = var14.length;

         for(int var16 = 0; var16 < var15; ++var16) {
            BlockFace direction2 = var14[var16];
            double d7 = direction2 != BlockFace.WEST && direction2 != BlockFace.EAST ? relativeZMovement : relativeXMovement;
            double d6 = direction2 != BlockFace.EAST && direction2 != BlockFace.SOUTH ? d7 : 1.0 - d7;
            boolean doesSuffocate;
            switch (1.$SwitchMap$org$bukkit$block$BlockFace[direction2.ordinal()]) {
               case 1:
                  doesSuffocate = suffocatesAt(blockX + 1, blockZ, data);
                  break;
               case 2:
                  doesSuffocate = suffocatesAt(blockX - 1, blockZ, data);
                  break;
               case 3:
                  doesSuffocate = suffocatesAt(blockX, blockZ - 1, data);
                  break;
               case 4:
               default:
                  doesSuffocate = suffocatesAt(blockX, blockZ + 1, data);
            }

            if (!(d6 >= lowestValue) && !doesSuffocate) {
               lowestValue = d6;
               direction = direction2;
            }
         }

         if (direction != null) {
            Location loc = data.getLocation().toLocation(data.getWorld());
            Location toSetback = null;
            Location locSubtract;
            Location locAddition;
            if (direction != BlockFace.WEST && direction != BlockFace.EAST) {
               locSubtract = loc.clone();
               locAddition = loc.clone();
               locSubtract.setZ(loc.getZ() - 0.1 * (double)direction.getModZ());
               locAddition.setZ(loc.getZ() + 0.1 * (double)direction.getModZ());
               if (BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                  Tasker.run(() -> {
                     data.getBukkitPlayer().teleport(locSubtract);
                  });
                  return locSubtract;
               }

               if (BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                  Tasker.run(() -> {
                     data.getBukkitPlayer().teleport(locAddition);
                  });
                  return locAddition;
               }
            } else {
               locSubtract = loc.clone();
               locAddition = loc.clone();
               locSubtract.setX(loc.getX() - 0.1 * (double)direction.getModX());
               locAddition.setX(loc.getX() + 0.1 * (double)direction.getModX());
               if (BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                  Tasker.run(() -> {
                     data.getBukkitPlayer().teleport(locSubtract);
                  });
                  return locSubtract;
               }

               if (BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                  Tasker.run(() -> {
                     data.getBukkitPlayer().teleport(locAddition);
                  });
                  return locAddition;
               }
            }
         }

         return null;
      }
   }

   public static boolean suffocatesAt(int x, int z, KarhuPlayer data) {
      BoundingBox boundingBox = (new BoundingBox(data, (double)x, data.getBoundingBox().minY, (double)z, (double)x + 1.0, data.getBoundingBox().maxY, (double)z + 1.0)).expand(-1.0E-7);
      return !boundingBox.getCollidingBlocks().isEmpty();
   }
}
