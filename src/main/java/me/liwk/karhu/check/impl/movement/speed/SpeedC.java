package me.liwk.karhu.check.impl.movement.speed;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.update.MovementUpdate;
import org.bukkit.util.Vector;

@CheckInfo(
   name = "Speed (C)",
   category = Category.MOVEMENT,
   subCategory = SubCategory.SPEED,
   experimental = false
)
public final class SpeedC extends PositionCheck {
   public SpeedC(KarhuPlayer data, Karhu karhu) {
      super(data, karhu);
   }

   public void handle(MovementUpdate e) {
      float friction = this.data.getCurrentFriction();
      float lastTickFriction = this.data.getLastTickFriction();
      Vector velocity = this.data.getTickedVelocity();
      if (!(this.data.deltas.deltaXZ < 0.1) && !(this.data.deltas.lastDX < 0.04) && !this.data.isOnLadder() && velocity == null && this.data.elapsed(this.data.getLastPistonPush()) > 3 && this.data.elapsed(this.data.getLastFlyTick()) > 30 && this.data.elapsed(this.data.getLastGlide()) > 30 && this.data.elapsed(this.data.getLastRiptide()) > 30 && !this.data.isOnLiquid() && !this.data.isInBed() && !this.data.isOnScaffolding() && !this.data.isLastInBed() && this.data.elapsed(this.data.getLastCollidedH()) >= 2 && this.data.elapsed(this.data.getLastCollidedGhost()) >= 2) {
         double lowestMatch = Double.MAX_VALUE;
         double predictX = 0.0;
         double predictZ = 0.0;
         int scenarios = 0;
         boolean onGround = e.from.ground;

         for(int forw = -1; forw < 2; ++forw) {
            for(int stra = -1; stra < 2; ++stra) {
               boolean[] var15 = BOOLEANS_REVERSED;
               int var16 = var15.length;

               for(int var17 = 0; var17 < var16; ++var17) {
                  boolean attack = var15[var17];
                  boolean[] var19 = BOOLEANS_REVERSED;
                  int var20 = var19.length;

                  for(int var21 = 0; var21 < var20; ++var21) {
                     boolean using = var19[var21];
                     boolean[] var23 = BOOLEANS;
                     int var24 = var23.length;

                     for(int var25 = 0; var25 < var24; ++var25) {
                        boolean sprinting = var23[var25];
                        boolean[] var27 = BOOLEANS_REVERSED;
                        int var28 = var27.length;

                        for(int var29 = 0; var29 < var28; ++var29) {
                           boolean sneaking = var27[var29];
                           boolean[] var31 = BOOLEANS_REVERSED;
                           int var32 = var31.length;

                           for(int var33 = 0; var33 < var32; ++var33) {
                              boolean jump = var31[var33];
                              ++scenarios;
                              float forward = (float)forw;
                              float strafe = (float)stra;
                              if (!attack || this.data.getLastAttackTick() <= 2 && this.data.getLastTarget() != null) {
                                 if (sneaking) {
                                    forward = (float)((double)forward * 0.3);
                                    strafe = (float)((double)strafe * 0.3);
                                 }

                                 if (using) {
                                    forward *= 0.2F;
                                    strafe *= 0.2F;
                                 }

                                 forward *= 0.98F;
                                 strafe *= 0.98F;
                                 float moveSpeed = this.data.getWalkSpeed();
                                 double lastDX = this.data.deltas.lastDX;
                                 double lastDZ = this.data.deltas.lastDZ;
                                 lastDX *= this.data.isLastLastOnGroundPacket() ? (double)lastTickFriction : 0.9100000262260437;
                                 lastDZ *= this.data.isLastLastOnGroundPacket() ? (double)lastTickFriction : 0.9100000262260437;
                                 if (attack) {
                                    lastDX *= 0.6;
                                    lastDZ *= 0.6;
                                 }

                                 if (Math.abs(lastDX) < this.data.clamp()) {
                                    lastDX = 0.0;
                                 }

                                 if (Math.abs(lastDZ) < this.data.clamp()) {
                                    lastDZ = 0.0;
                                 }

                                 if (sprinting) {
                                    moveSpeed += moveSpeed * 0.3F;
                                 }

                                 float f5;
                                 float inputForce;
                                 if (onGround) {
                                    f5 = moveSpeed * (0.16277136F / (friction * friction * friction));
                                    if (jump && sprinting) {
                                       inputForce = e.to.yaw * 0.017453292F;
                                       lastDX -= (double)(MathHelper.sin(inputForce) * 0.2F);
                                       lastDZ += (double)(MathHelper.cos(inputForce) * 0.2F);
                                    }
                                 } else {
                                    f5 = sprinting ? 0.026F : 0.02F;
                                 }

                                 inputForce = forward * forward + strafe * strafe;
                                 if (inputForce >= 1.0E-4F) {
                                    inputForce = MathHelper.sqrt_float(inputForce);
                                    if (inputForce < 1.0F) {
                                       inputForce = 1.0F;
                                    }

                                    inputForce = f5 / inputForce;
                                    forward *= inputForce;
                                    strafe *= inputForce;
                                    float yawShit = e.to.yaw * 3.1415927F / 180.0F;
                                    float yawSin = MathHelper.sin(yawShit);
                                    float yawCos = MathHelper.cos(yawShit);
                                    lastDX += (double)(strafe * yawCos - forward * yawSin);
                                    lastDZ += (double)(forward * yawCos + strafe * yawSin);
                                 }

                                 double moveDiff = MathUtil.hypot(this.data.deltas.deltaX - lastDX, this.data.deltas.deltaZ - lastDZ);
                                 if (moveDiff < lowestMatch) {
                                    lowestMatch = moveDiff;
                                    predictX = lastDX;
                                    predictZ = lastDZ;
                                 }

                                 lowestMatch = Math.min(moveDiff, lowestMatch);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         double predicted = MathUtil.hypot(predictX, predictZ);
         predicted += this.getLeniency();
         boolean invalid = lowestMatch > 0.005;
         double tMult = Math.max(Karhu.getInstance().getConfigManager().getSpeedCMult(), 1.0001);
         if (invalid && this.data.deltas.deltaXZ > predicted * tMult) {
            if (++this.violations > 6.0) {
               this.fail("* Prediction\n §f* match §b" + lowestMatch + "\n §f* predict §b" + predicted + "\n §f* scenarios §b" + scenarios + "\n §f* move §b" + this.data.deltas.deltaXZ, 300L);
            }

            this.debug(String.format("match: %s §fbuffer: %.1f", lowestMatch, this.violations));
         } else {
            this.decrease(0.075);
         }

      } else {
         this.decrease(0.005);
      }
   }

   private double getLeniency() {
      double leniency = 0.0;
      boolean entity = this.data.elapsed(this.data.getLastCollidedWithEntity()) <= 10;
      if (this.data.getMoveTicks() <= 3) {
         leniency += this.data.offsetMove() + 0.001;
      }

      if (this.data.elapsed(this.data.getLastSneakEdge()) <= 5) {
         leniency += 0.15;
      }

      if (this.data.elapsed(this.data.getLastOnSoul()) <= 3) {
         leniency += 0.05;
      }

      if (this.data.elapsed(this.data.getLastOnSlime()) <= 3) {
         leniency += 0.05;
      }

      if (this.data.elapsed(this.data.getLastInBerry()) <= 3) {
         leniency += 0.01;
      }

      if (this.data.isOnHoney() || this.data.isWasOnHoney()) {
         leniency += 0.05;
      }

      if (this.data.isInWeb() || this.data.isWasInWeb()) {
         leniency += 0.25;
      }

      if (this.data.elapsed(this.data.getLastInPowder()) <= 3) {
         leniency += 0.25;
      }

      if (this.data.elapsed(this.data.getLastCollidedGhost()) <= 2) {
         leniency += 0.3;
      }

      if (entity) {
         leniency += 0.1;
      }

      return leniency;
   }
}
