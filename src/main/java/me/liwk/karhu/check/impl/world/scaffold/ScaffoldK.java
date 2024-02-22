package me.liwk.karhu.check.impl.world.scaffold;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.BlockPlaceEvent;
import me.liwk.karhu.event.Event;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@CheckInfo(
   name = "Scaffold (K)",
   category = Category.WORLD,
   subCategory = SubCategory.SCAFFOLD,
   experimental = true
)
public final class ScaffoldK extends PacketCheck {
   public ScaffoldK(KarhuPlayer data, Karhu karhu) {
      super(data, karhu);
   }

   public void handle(Event packet) {
      if (packet instanceof BlockPlaceEvent) {
         BlockPlaceEvent place = (BlockPlaceEvent)packet;
         ItemStack stack = place.getItemStack() == null ? new ItemStack(Material.AIR) : place.getItemStack();
         int face = ((BlockPlaceEvent)packet).getFace();
         if (face >= 0 && face <= 6) {
            if (!place.isUsableItem() && stack.getType().isSolid() && stack.getType().isBlock()) {
               Vector blockPlaced = place.getOrigin();
               Vector blockClicked = place.getBlockPos();
               double blockX = blockClicked.getX();
               double targetX = blockPlaced.getX();
               double blockZ = blockClicked.getZ();
               double targetZ = blockPlaced.getZ();
               double diffX = blockX - targetX;
               double diffZ = blockZ - targetZ;
               if (!(Math.abs(diffX) + Math.abs(diffZ) > 1.0) && blockClicked.getY() == blockPlaced.getY() && !(this.data.getLocation().getY() < (double)(blockClicked.getBlockY() + 1)) && !this.data.isOnClimbable() && !this.data.isNearClimbable() && !this.data.isInsideTrapdoor() && !this.data.isCollidedHorizontally() && !this.data.isRiding() && !this.data.isAtButton() && !this.data.isOnFence() && !this.data.isOnStairs() && !(this.data.deltas.deltaXZ < 0.07) && !this.data.isOnSlab()) {
                  diffX *= 0.5;
                  diffZ *= 0.5;
                  double combined;
                  double distance;
                  double multiplied;
                  double rate;
                  if (Math.abs(diffX) > 0.0) {
                     combined = targetX + 0.5 + diffX;
                     distance = this.data.getLocation().getX() - combined;
                     multiplied = distance * diffX;
                     rate = Math.max(1.1, Math.ceil(Math.abs(multiplied) * 50.0));
                     if (multiplied < 0.0 && this.isNotGroundBridging(blockClicked)) {
                        if ((this.violations += rate) > 2.0) {
                           this.fail("* Impossible block place (X-AXIS)\n §f* combined: §b" + combined + "\n §f* distance: §b" + distance + "\n §f* multiplied: §b" + multiplied, this.getBanVL(), 200L);
                        }
                     } else {
                        this.decrease(0.05);
                     }
                  }

                  if (Math.abs(diffZ) > 0.0) {
                     combined = targetZ + 0.5 + diffZ;
                     distance = this.data.getLocation().getZ() - combined;
                     multiplied = distance * diffZ;
                     rate = Math.max(1.1, Math.ceil(Math.abs(multiplied) * 50.0));
                     if (multiplied < 0.0 && this.isNotGroundBridging(blockClicked)) {
                        if ((this.violations += rate) > 1.0) {
                           this.fail("* Impossible block place (Z-AXIS)\n §f* combined: §b" + combined + "\n §f* distance: §b" + distance + "\n §f* multiplied: §b" + multiplied, this.getBanVL(), 200L);
                           return;
                        }
                     } else {
                        this.decrease(0.05);
                     }

                  }
               }
            } else {
               this.decrease(0.15);
            }
         }
      }
   }

   public boolean isNotGroundBridging(Vector blockLoc) {
      Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(this.data.getLocation().clone().subtract(0.0, 2.0, 0.0).toLocation(this.data.getWorld()));
      Block block2 = Karhu.getInstance().getChunkManager().getChunkBlockAt(blockLoc.toLocation(this.data.getWorld()));
      if (block != null && block2 != null) {
         return block.getType() == Material.AIR;
      } else {
         return false;
      }
   }
}
