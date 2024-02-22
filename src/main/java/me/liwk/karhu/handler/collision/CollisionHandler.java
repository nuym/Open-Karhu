package me.liwk.karhu.handler.collision;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.collision.enums.Boxes;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.handler.interfaces.KarhuHandler;
import me.liwk.karhu.util.KarhuStream;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.benchmark.Benchmark;
import me.liwk.karhu.util.benchmark.BenchmarkType;
import me.liwk.karhu.util.benchmark.KarhuBenchmarker;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public final class CollisionHandler implements KarhuHandler {
   private final KarhuPlayer data;
   private List cache1_13 = new CopyOnWriteArrayList();

   public void handleLastTicks() {
      this.data.setWasOnBed(this.data.isOnBed());
      this.data.setWasOnDoor(this.data.isOnDoor());
      this.data.setWasWasInWeb(this.data.isWasInWeb());
      this.data.setWasInWeb(this.data.isInWeb());
      this.data.setWasWasOnSlime(this.data.isWasOnSlime());
      this.data.setWasOnSlime(this.data.isOnSlime());
      this.data.setWasSlimeLand(this.data.isSlimeLand());
      this.data.setWasOnSlab(this.data.isOnStairs());
      this.data.setWasOnSlab(this.data.isOnSlab());
      this.data.setWasOnHoney(this.data.isOnHoney());
      this.data.setWasOnSoulSand(this.data.isOnSoulsand());
      this.data.setWasUnderBlock(this.data.isUnderBlock());
      this.data.setWasOnFence(this.data.isOnFence());
      this.data.setWasOnWater(this.data.isOnWater());
      this.data.setWasOnLava(this.data.isOnLava());
      this.data.setWasWasOnClimbable(this.data.isWasOnClimbable());
      this.data.setWasOnClimbable(this.data.isOnClimbable());
      this.data.setWasFullyInsideBlock(this.data.isFullyInsideBlock());
      this.data.setWasOnComparator(this.data.isOnComparator());
      this.data.setWasOnGroundServer(this.data.isOnGroundServer());
   }

   public void cacheBlocks() {
      double areaSize = this.data.elapsed(this.data.getUnderPlaceTicks()) <= 3 + MathUtil.getPingInTicks(this.data.getTransactionPing()) ? 2.0 : Math.min(3.0, this.data.deltas.deltaXZ + 0.13);
      this.cache1_13 = this.data.getBoundingBox().clone().expand(areaSize, 2.0, areaSize).getCollidingBlocks();
   }

   public void handle(boolean moved) {
      long nanoStart = System.nanoTime();
      boolean newVer = Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_13);
      double areaSize = this.data.elapsed(this.data.getUnderPlaceTicks()) <= 3 + MathUtil.getPingInTicks(this.data.getTransactionPing()) ? 2.0 : Math.min(3.0, this.data.deltas.deltaXZ + 0.13);
      List tickCache = newVer ? this.cache1_13 : this.data.getBoundingBox().clone().expand(areaSize, 2.0, areaSize).getCollidingBlocks();
      double substract = newVer ? 0.1 : 0.031;
      BoundingBox below = this.data.getBoundingBox().clone().subtractMin(0.0, substract, 0.0);
      BoundingBox belowHugeBox = this.data.getBoundingBox().clone().expand(areaSize, 0.0, areaSize).subtractMin(0.0, 3.0, 0.0).subtractMax(0.0, 1.805, 0.0);
      BoundingBox inside = this.data.getBoundingBox().clone().expandMin(0.01, 0.8, 0.01).subtractMax(0.01, 0.8, 0.01);
      BoundingBox mcpWaterBox = this.data.getMcpCollision().clone().expand(0.0, -0.4000000059604645, 0.0);
      BoundingBox mcpLavaBox = this.data.getBoundingBox().clone().expand(-0.10000000149011612, -0.4000000059604645, -0.10000000149011612);
      BoundingBox mcpAnyLiquidBox = this.data.getBoundingBox().clone().translate(this.data.deltas.deltaX, this.data.deltas.lastMotionY * 0.800000011920929 + 0.6000000238418579, this.data.deltas.deltaZ);
      if (this.data.deltas.motionY == 0.0 && this.data.isOnGroundPacket() && this.data.getLocation().getY() - (double)MathHelper.floor_double(this.data.getLocation().getY()) == 0.0625) {
         below.subtractMin(0.0, 0.05, 0.0);
      }

      BoundingBox above = this.data.getBoundingBox().clone().expandMin(0.0, 1.795, 0.0).expand(new Vector(-this.data.deltas.deltaX, 0.0, -this.data.deltas.deltaZ)).expandMax(0.0, 0.4, 0.0).expand(0.005, 0.0, 0.005);
      BoundingBox aboveStrict = this.data.getBoundingBox().clone().expandMin(0.0, 1.795, 0.0).expandMax(0.0, 0.3, 0.0);
      BoundingBox lqBelow = this.data.getBoundingBox().clone().subtractMax(0.0, 1.8, 0.0).subtractMin(0.0, 0.0425, 0.0);
      BoundingBox horiBox = this.data.getBoundingBox().clone().expandMin(0.0, 0.2, 0.0).expand(0.01, 0.0, 0.01);
      KarhuStream blocksBelow = new KarhuStream(below.getCachedCollidingBlocks(tickCache));
      KarhuStream blocksNearHugeBelow = new KarhuStream(belowHugeBox.getCachedCollidingBlocks(tickCache));
      KarhuStream blocksInsideMiddleBox = new KarhuStream(inside.getCachedCollidingBlocks(tickCache));
      KarhuStream normalBox = new KarhuStream(this.data.getBoundingBox().getCachedCollidingBlocks(tickCache));
      KarhuStream lqStream = new KarhuStream(lqBelow.getCachedCollidingBlocks(tickCache));
      KarhuStream mcpLandStream = new KarhuStream(this.data.getBoundingBox().clone().getCollidingOnLanded(tickCache, (double)MathHelper.floor_double(this.data.getLocation().getX()), (double)MathHelper.floor_double(this.data.getLocation().getY() - 0.20000000298023224), (double)MathHelper.floor_double(this.data.getLocation().getZ())));
      KarhuStream mcpWaterStream = new KarhuStream(mcpWaterBox.getCollidingMaterialAccel(tickCache));
      KarhuStream mcpLavaStream = new KarhuStream(mcpLavaBox.getCollidingMaterialAccel(tickCache));
      KarhuStream blocksHori = new KarhuStream(horiBox.getCachedCollidingBlocks(tickCache));
      KarhuStream blocksAbove = new KarhuStream(above.getCachedCollidingBlocks(tickCache));
      KarhuStream blocksAboveStrict = new KarhuStream(aboveStrict.getCachedCollidingBlocks(tickCache));
      KarhuStream entities = null;
      KarhuStream entities2 = null;
      if (Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_19)) {
         entities = new KarhuStream(this.data.getBoundingBox().clone().expand(0.3, 0.6, 0.3).getCollidingEntities());
      } else {
         entities2 = new KarhuStream(this.data.getBoundingBox().clone().expand(0.3, 0.6, 0.3).getCollidingEntitiesNew());
      }

      KarhuStream entitiesUp = null;
      KarhuStream entitiesUp2 = null;
      if (Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_19)) {
         entitiesUp = new KarhuStream(this.data.getBoundingBox().clone().expandMin(0.0, 1.5, 0.0).expandMax(0.0, 0.7, 0.0).expand(0.65, 0.0, 0.65).getCollidingEntities());
      } else {
         entitiesUp2 = new KarhuStream(this.data.getBoundingBox().clone().expandMin(0.0, 1.5, 0.0).expandMax(0.0, 0.7, 0.0).expand(0.65, 0.0, 0.65).getCollidingEntitiesNew());
      }

      long nanoStartCollision = System.nanoTime();
      boolean fence = blocksBelow.any((m) -> {
         return MaterialChecks.FENCES.contains(m) || MaterialChecks.MOVABLE.contains(m);
      });
      boolean piston = blocksBelow.any((m) -> {
         return MaterialChecks.MOVABLE.contains(m);
      });
      boolean entity = Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_19) ? entities.any((e) -> {
         return e.getType() == EntityType.BOAT;
      }) : entities2.any((e) -> {
         return EntityTypes.BOAT.equals(e) || EntityTypes.CHEST_BOAT.equals(e);
      });
      boolean livingEntity = Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_19) ? entities.any((e) -> {
         return e instanceof LivingEntity;
      }) : entities2.any((e) -> {
         return EntityTypes.isTypeInstanceOf(e, EntityTypes.LIVINGENTITY);
      });
      this.data.setCollidedWithLivingEntity(livingEntity);
      this.data.setOnBoat(entity);
      this.data.setOnGroundServer(entity || fence || piston || blocksBelow.any((b) -> {
         return b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b);
      }));
      this.data.setGroundNearBox(entity || fence || piston || blocksNearHugeBelow.any((m) -> {
         return m.isSolid() || MaterialChecks.WEIRD_SOLID.contains(m);
      }));
      this.data.setOnSoulsand(blocksBelow.any((b) -> {
         return MaterialChecks.SOUL.contains(b);
      }));
      this.data.setCollidedHorizontalClient(normalBox.any((b) -> {
         return b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b);
      }));
      this.data.setOnStairs(blocksBelow.any((b) -> {
         return MaterialChecks.STAIRS.contains(b);
      }) || blocksHori.any((b) -> {
         return MaterialChecks.STAIRS.contains(b);
      }));
      this.data.setOnDoor(blocksBelow.any((b) -> {
         return MaterialChecks.DOORS.contains(b);
      }));
      this.data.setOnSlab(blocksBelow.any((b) -> {
         return MaterialChecks.HALFS.contains(b);
      }) || blocksHori.any((b) -> {
         return MaterialChecks.HALFS.contains(b);
      }));
      this.data.setOnWeb(blocksBelow.any((b) -> {
         return MaterialChecks.WEB.contains(b);
      }));
      this.data.setOnCarpet(blocksBelow.any((b) -> {
         return MaterialChecks.CARPETS.contains(b);
      }));
      this.data.setOnComparator(blocksBelow.any((b) -> {
         return MaterialChecks.REDSTONE.contains(b);
      }));
      this.data.setOnHoney(blocksBelow.any((b) -> {
         return MaterialChecks.HONEY.contains(b);
      }));
      this.data.setOnScaffolding(blocksBelow.any((b) -> {
         return MaterialChecks.SCAFFOLD.contains(b);
      }));
      if (!this.data.isInUnloadedChunk()) {
         this.data.setOnWater(mcpWaterStream.any((b) -> {
            return MaterialChecks.WATER.contains(b) || MaterialChecks.SEASHIT.contains(b);
         }));
         this.data.setOnLava(mcpLavaStream.any((b) -> {
            return MaterialChecks.LAVA.contains(b);
         }));
         this.data.setLastOnWaterOffset(this.data.isOnWaterOffset());
         this.data.setOnWaterOffset(mcpAnyLiquidBox.getAnyLiquid(tickCache));
      }

      if (this.data.isNewerThan13()) {
         CustomLocation location = this.data.getLastLocation();
         float width = Boxes.CROUCH.getWidth();
         float height = Boxes.CROUCH.getHeight();
         BoundingBox bbCrouch = new BoundingBox(this.data, location.x - (double)width, location.y, location.z - (double)width, location.x + (double)width, location.y + (double)height, location.z + (double)width);
         this.data.setCrouching(bbCrouch.getCachedCollidingBlocks(tickCache).isEmpty() && (this.data.isWasSneaking() || !this.data.getLastBoundingBox().getCollidingBlocks().isEmpty()));
      }

      this.data.setOnSweet(normalBox.any((b) -> {
         return MaterialChecks.BERRIES.contains(b);
      }));
      this.data.setOnClimbable(normalBox.any((b) -> {
         return MaterialChecks.CLIMBABLE.contains(b);
      }));
      this.data.setFullyInsideBlock(normalBox.any((b) -> {
         return b.isSolid() && !MaterialChecks.SIGNS.contains(b) || this.data.isOnClimbable();
      }));
      this.data.setInsideTrapdoor(blocksBelow.any((b) -> {
         return MaterialChecks.TRAPS.contains(b);
      }));
      this.data.setInWeb(normalBox.any((material) -> {
         return MaterialChecks.WEB.contains(material);
      }));
      this.data.setInPowder(normalBox.any((material) -> {
         return MaterialChecks.POWDERSNOW.contains(material);
      }));
      this.data.setOnBed(blocksHori.any((b) -> {
         return MaterialChecks.BED.contains(b);
      }));
      this.data.setAtButton(blocksHori.any((b) -> {
         return MaterialChecks.RETARD_FACE.contains(b);
      }));
      this.data.setNearClimbable(blocksHori.any((b) -> {
         return MaterialChecks.CLIMBABLE.contains(b);
      }));
      this.data.setCollidedWithCactus(blocksHori.any((b) -> {
         return b == Material.CACTUS;
      }));
      this.data.setCollidedWithFence(blocksHori.any((b) -> {
         return MaterialChecks.FENCES.contains(b);
      }));
      this.data.setCollidedWithPane(blocksHori.any((b) -> {
         return MaterialChecks.PANES.contains(b);
      }));
      boolean hori = blocksHori.any((b) -> {
         return b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b);
      }) || this.data.isOnClimbable();
      this.data.setCollidedHorizontally(hori);
      this.data.setFinalCollidedH(hori);
      this.data.setAtButton(blocksHori.any((b) -> {
         return MaterialChecks.RETARD_FACE.contains(b);
      }));
      this.data.setAtSign(blocksHori.any((b) -> {
         return MaterialChecks.RETARD_FACE.contains(b);
      }));
      this.data.setNearClimbable(blocksHori.any((b) -> {
         return MaterialChecks.CLIMBABLE.contains(b);
      }));
      if (!this.data.isAtButton()) {
         this.data.setAtButton(blocksNearHugeBelow.any((b) -> {
            return MaterialChecks.RETARD_FACE.contains(b);
         }));
      }

      if (!this.data.isAtSign()) {
         this.data.setAtSign(blocksNearHugeBelow.any((b) -> {
            return MaterialChecks.RETARD_FACE.contains(b);
         }));
      }

      if (!this.data.isNearClimbable()) {
         this.data.setNearClimbable(blocksNearHugeBelow.any((b) -> {
            return MaterialChecks.CLIMBABLE.contains(b);
         }));
      }

      this.data.setOnIce(blocksNearHugeBelow.any((b) -> {
         return MaterialChecks.ICE.contains(b);
      }));
      if (Karhu.SERVER_VERSION.getProtocolVersion() >= 47) {
         this.data.setOnSlime(blocksNearHugeBelow.any((b) -> {
            return MaterialChecks.SLIME.contains(b);
         }));
         this.data.setSlimeLand(mcpLandStream.any((b) -> {
            return MaterialChecks.SLIME.contains(b);
         }));
      }

      this.data.setOnPiston(piston);
      this.data.setOnFence(fence);
      boolean water = lqStream.any((material) -> {
         return MaterialChecks.WATER.contains(material) || MaterialChecks.SEASHIT.contains(material);
      });
      boolean lily = lqStream.any((material) -> {
         return MaterialChecks.LILY.contains(material);
      });
      boolean waterBelow = water && !lily;
      if (!this.data.isInUnloadedChunk()) {
         this.data.setAboveButNotInWater(waterBelow);
      } else {
         this.data.setOnWater(false);
         this.data.setOnLava(false);
         this.data.setOnLiquid(false);
      }

      this.data.setOnLiquid(this.data.isWasOnWater() || this.data.isOnLava());
      Location climbable = this.onClimbable();
      Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(climbable);
      Block sneakBlock = Karhu.getInstance().getChunkManager().getChunkBlockAt(this.data.getLastLocation().toLocation(this.data.getWorld()).add(0.0, -0.2, 0.0));
      boolean isRightBlock = sneakBlock != null && !sneakBlock.getType().isSolid() && !MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(sneakBlock.getType());
      this.data.setLastLadder(this.data.isOnLadder());
      if (block != null) {
         this.data.setOnLadder(MaterialChecks.CLIMBABLE.contains(block.getType()));
      }

      if (!isRightBlock && !this.data.isLastBlockSneak()) {
         this.data.setSneakEdge(false);
      } else {
         this.data.setSneakEdge(this.data.getAirTicks() <= 3 && this.data.isOnGroundPacket());
      }

      this.data.setLastBlockSneak(isRightBlock);
      boolean boatsUp = Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_19) ? entitiesUp.any((e) -> {
         return e.getType() == EntityType.BOAT;
      }) : entitiesUp2.any((e) -> {
         return EntityTypes.BOAT.equals(e) || EntityTypes.CHEST_BOAT.equals(e);
      });
      this.data.setUnderBlock(blocksAbove.any((b) -> {
         return b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b) || boatsUp;
      }));
      this.data.setUnderBlockStrict(blocksAboveStrict.any((b) -> {
         return b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b) || boatsUp;
      }));
      this.data.setUnderWeb(blocksAbove.any((b) -> {
         return MaterialChecks.WEB.contains(b);
      }));
      this.data.setInsideBlock(blocksInsideMiddleBox.any((b) -> {
         return (b.isSolid() || MaterialChecks.WEIRD_SOLID_NO_LIQUID.contains(b)) && !MaterialChecks.SIGNS.contains(b) && !MaterialChecks.CLIMBABLE.contains(b);
      }));
      long nanoStop = System.nanoTime();
      Benchmark cacheData = KarhuBenchmarker.getProfileData(BenchmarkType.BLOCK_CACHE);
      cacheData.insertResult(nanoStart, nanoStartCollision);
      Benchmark profileData = KarhuBenchmarker.getProfileData(BenchmarkType.BLOCK_COLLISION);
      profileData.insertResult(nanoStartCollision, nanoStop);
   }

   public void handleTicks() {
      boolean half = this.data.isOnStairs() || this.data.isOnSlab() || this.data.isOnBed() || this.data.isInsideTrapdoor() || this.data.isOnFence();
      this.data.setLastCollided(!this.data.isCollidedHorizontally() && !this.data.isUnderBlock() ? this.data.getLastCollided() : this.data.getTotalTicks());
      this.data.setLastCollidedH(this.data.isCollidedHorizontally() ? this.data.getTotalTicks() : this.data.getLastCollidedH());
      this.data.setLastCollidedV(this.data.isUnderBlock() ? this.data.getTotalTicks() : this.data.getLastCollidedV());
      this.data.setLastInLiquidOffset(this.data.isOnWaterOffset() ? this.data.getTotalTicks() : this.data.getLastInLiquidOffset());
      this.data.setLastInLiquid(this.data.isOnLiquid() ? this.data.getTotalTicks() : this.data.getLastInLiquid());
      this.data.setLastOnSlime(this.data.isOnSlime() ? this.data.getTotalTicks() : this.data.getLastOnSlime());
      this.data.setLastOnSoul(this.data.isOnSoulsand() ? this.data.getTotalTicks() : this.data.getLastOnSoul());
      this.data.setLastOnIce(this.data.isOnIce() ? this.data.getTotalTicks() : this.data.getLastOnIce());
      this.data.setLastOnClimbable(this.data.isOnClimbable() ? this.data.getTotalTicks() : this.data.getLastOnClimbable());
      this.data.setLastOnBed(this.data.isOnBed() ? this.data.getTotalTicks() : this.data.getLastOnBed());
      this.data.setLastOnHalfBlock(half ? this.data.getTotalTicks() : this.data.getLastOnHalfBlock());
      this.data.setLastFence(this.data.isOnFence() ? this.data.getTotalTicks() : this.data.getLastFence());
      this.data.setLastInWeb(!this.data.isOnWeb() && !this.data.isInWeb() ? this.data.getLastInWeb() : this.data.getTotalTicks());
      this.data.setLastInBerry(this.data.isOnSweet() ? this.data.getTotalTicks() : this.data.getLastInBerry());
      this.data.setLastInPowder(this.data.isInPowder() ? this.data.getTotalTicks() : this.data.getLastInPowder());
      if (this.data.isSneakEdge()) {
         this.data.setLastSneakEdge(this.data.getTotalTicks());
      }

      if (Karhu.SERVER_VERSION.getProtocolVersion() > 47) {
         this.data.setLastCollidedWithEntity(this.data.isCollidedWithLivingEntity() ? this.data.getTotalTicks() : this.data.getLastCollidedWithEntity());
      }

      this.data.setLastOnBoat(this.data.isOnBoat() ? this.data.getTotalTicks() : this.data.getLastOnBoat());
      this.data.setServerGroundTicks(this.data.isOnGroundServer() ? this.data.getServerGroundTicks() + 1 : 0);
      this.data.setClientGroundTicks(this.data.isOnGroundPacket() ? this.data.getClientGroundTicks() + 1 : 0);
   }

   private Location onClimbable() {
      int i = MathHelper.floor_double(this.data.getLocation().getX());
      int j = MathHelper.floor_double(this.data.getLocation().getY());
      int k = MathHelper.floor_double(this.data.getLocation().getZ());
      Location loc = new Location(this.data.getWorld(), (double)i, (double)j, (double)k);
      return loc;
   }

   public CollisionHandler(KarhuPlayer data) {
      this.data = data;
   }
}
