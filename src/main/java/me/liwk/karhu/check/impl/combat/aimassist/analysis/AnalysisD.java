package me.liwk.karhu.check.impl.combat.aimassist.analysis;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.RotationCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(
   name = "Analysis (D)",
   category = Category.COMBAT,
   subCategory = SubCategory.AIM,
   experimental = true
)
public class AnalysisD extends RotationCheck {
   private final Deque pitchMatchList = new LinkedList();
   private final Deque yawMatchList = new LinkedList();

   public AnalysisD(KarhuPlayer data, Karhu karhu) {
      super(data, karhu);
   }

   public void handle(MovementUpdate update) {
      if (this.data.getLastAttackTick() <= 1 && this.data.getLastTarget() != null && this.data.deltas.deltaXZ > 0.1) {
         EntityData edata = (EntityData)this.data.getEntityData().get(this.data.getLastTarget().getEntityId());
         if (edata != null) {
            AxisAlignedBB entityBB = edata.getEntityBoundingBox();
            float deltaYaw = this.data.deltas.deltaYaw;
            float deltaPitch = this.data.deltas.deltaPitch;
            float[] rotationBasic = this.getRotations(update.from, entityBB);
            float delta;
            if (deltaYaw > 0.0F) {
               delta = MathUtil.getAngleDistance(rotationBasic[0], update.to.yaw);
               this.yawMatchList.add(delta);
            }

            if (deltaPitch > 0.0F) {
               delta = MathUtil.getAngleDistance(rotationBasic[1], update.to.pitch);
               this.pitchMatchList.add(delta);
            }
         }
      }

      Deque closes;
      int matches;
      double average;
      if (this.yawMatchList.size() == 150) {
         closes = (Deque)this.yawMatchList.stream().filter((deltax) -> {
            return deltax <= 2.0F;
         }).collect(Collectors.toCollection(LinkedList::new));
         matches = closes.size();
         if (matches >= 110) {
            average = MathUtil.getAverage(closes);
            this.fail("* Rotation analysis (common, yaw)\n §f* avg: §b" + average + "\n §f* rate: §b" + matches, this.getBanVL(), 300L);
         }

         this.yawMatchList.clear();
      }

      if (this.pitchMatchList.size() == 150) {
         closes = (Deque)this.pitchMatchList.stream().filter((deltax) -> {
            return deltax <= 2.0F;
         }).collect(Collectors.toCollection(LinkedList::new));
         matches = closes.size();
         if (matches >= 110) {
            average = MathUtil.getAverage(closes);
            this.fail("* Rotation analysis (common, pitch)\n §f* avg: §b" + average + "\n §f* rate: §b" + matches, this.getBanVL(), 300L);
         }

         this.pitchMatchList.clear();
      }

   }

   private float[] getRotations(CustomLocation playerLocation, AxisAlignedBB aabb) {
      double xDiff = aabb.getCenterX() - playerLocation.getX();
      double zDiff = aabb.getCenterZ() - playerLocation.getZ();
      double yDiff = aabb.minY - (playerLocation.getY() + 1.62);
      double dist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
      float yaw = (float)(Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-(Math.atan2(yDiff, dist) * 180.0 / Math.PI));
      return new float[]{yaw, pitch};
   }
}
