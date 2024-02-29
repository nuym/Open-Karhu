package me.liwk.karhu.check.setback;

import lombok.SneakyThrows;
import me.liwk.karhu.check.setback.Setbacks;
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

   @SneakyThrows
   public static CustomLocation forgeToRotatedLocation(CustomLocation loc, KarhuPlayer data) {
      CustomLocation locMod = loc.clone();
      locMod.setYaw(data.getLocation().yaw);
      locMod.setPitch(data.getLocation().pitch);
      return locMod;
   }

   public static Location moveOutOfBlockSafely(double x, double z, KarhuPlayer data) {
      int blockX = MathHelper.floor(x);
      int blockZ = MathHelper.floor(z);

      // 检查给定位置是否导致窒息
      if (!suffocatesAt(blockX, blockZ, data)) {
         return null;
      } else {
         double relativeXMovement = x - blockX;
         double relativeZMovement = z - blockZ;
         BlockFace direction = null;
         double lowestValue = Double.MAX_VALUE;

         // 遍历四个方向
         for (BlockFace direction2 : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH}) {
            double d7 = (direction2 != BlockFace.WEST && direction2 != BlockFace.EAST) ? relativeZMovement : relativeXMovement;
            double d6 = (direction2 != BlockFace.EAST && direction2 != BlockFace.SOUTH) ? d7 : (1.0 - d7);
            boolean doesSuffocate;
            // 判断方向是否会导致窒息
            switch (direction2) {
               case EAST:
                  doesSuffocate = suffocatesAt(blockX + 1, blockZ, data);
                  break;
               case WEST:
                  doesSuffocate = suffocatesAt(blockX - 1, blockZ, data);
                  break;
               case NORTH:
                  doesSuffocate = suffocatesAt(blockX, blockZ - 1, data);
                  break;
               case SOUTH:
               default:
                  doesSuffocate = suffocatesAt(blockX, blockZ + 1, data);
            }

            // 找到不会导致窒息的方向
            if (!(d6 >= lowestValue) && !doesSuffocate) {
               lowestValue = d6;
               direction = direction2;
            }
         }

         // 移动到安全的位置
         if (direction != null) {
            Location loc = data.getLocation().toLocation(data.getWorld());
            Location toSetback = null;
            if (direction != BlockFace.WEST && direction != BlockFace.EAST) {
               Location locSubtract = loc.clone();
               Location locAddition = loc.clone();
               locSubtract.setZ(loc.getZ() - 0.1 * direction.getModZ());
               locAddition.setZ(loc.getZ() + 0.1 * direction.getModZ());
               if (BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                  Tasker.run(() -> data.getBukkitPlayer().teleport(locSubtract));
                  return locSubtract;
               }

               if (BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                  Tasker.run(() -> data.getBukkitPlayer().teleport(locAddition));
                  return locAddition;
               }
            } else {
               Location locSubtract = loc.clone();
               Location locAddition = loc.clone();
               locSubtract.setX(loc.getX() - 0.1 * direction.getModX());
               locAddition.setX(loc.getX() + 0.1 * direction.getModX());
               if (BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                  Tasker.run(() -> data.getBukkitPlayer().teleport(locSubtract));
                  return locSubtract;
               }

               if (BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                  Tasker.run(() -> data.getBukkitPlayer().teleport(locAddition));
                  return locAddition;
               }
            }
         }

         return null;
      }
   }


   public static boolean suffocatesAt(int x, int z, KarhuPlayer data) {
      BoundingBox boundingBox = new BoundingBox(
            data, (double)x, data.getBoundingBox().minY, (double)z, (double)x + 1.0, data.getBoundingBox().maxY, (double)z + 1.0
         )
         .expand(-1.0E-7);
      return !boundingBox.getCollidingBlocks().isEmpty();
   }
}
