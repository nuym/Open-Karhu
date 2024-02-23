package me.liwk.karhu.world.nms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.pair.Pair;

public final class NMSValueParser {
   public static final List KEY_COMBOS = Collections.synchronizedList(Arrays.asList(new float[]{1.0F, -1.0F}, new float[]{1.0F, 0.0F}, new float[]{1.0F, 1.0F}, new float[]{0.0F, -1.0F}, new float[]{0.0F, 0.0F}, new float[]{0.0F, 1.0F}, new float[]{-1.0F, -1.0F}, new float[]{-1.0F, 0.0F}, new float[]{-1.0F, 1.0F}));
   public static final boolean[] BOOLEANS = new boolean[]{true, false};
   public static final boolean[] BOOLEANS_REVERSED = new boolean[]{false, true};

   public static void parse(KarhuPlayer data) {
      data.setLastJumpMovementFactor(data.getJumpMovementFactor());
      data.setJumpMovementFactor(data.getSpeedInAir());
      data.setJumpMovementFactor(data.isWasSprinting() ? (float)((double)data.getJumpMovementFactor() + 0.005999999865889549) : data.getJumpMovementFactor());
      float attri = data.getWalkSpeed();
      if (data.isSprinting()) {
         attri += attri * 0.3F;
      }

      data.setLastAttributeSpeed(data.getAttributeSpeed());
      data.setAttributeSpeed(attri);
      double jumpMotion = 0.41999998688697815;
      jumpMotion += (double)((float)data.getJumpBoost() * 0.1F);
      double difference = data.deltas.motionY - jumpMotion;
      data.setJumpedLastTick(data.isJumpedCurrentTick());
      data.setJumpedCurrentTick(Math.abs(difference) <= 0.03125 && data.isLastOnGroundPacket() && !data.isOnGroundPacket() || data.isLastOnGroundPacket() && !data.isOnGroundPacket() && data.elapsed(data.getLastCollidedV()) <= 1 && data.deltas.motionY > 0.0 || data.recentlyTeleported(2));
      data.setJumped(data.isJumpedCurrentTick());
   }

   public static double moveFlying(KarhuPlayer data, float strafe, float forward, boolean sprint) {
      float friction = (Float)moveEntityWithHeading(data, sprint).getY();
      float f = strafe * strafe + forward * forward;
      if (f >= 1.0E-4F) {
         f = MathHelper.sqrt_float(f);
         if (f < 1.0F) {
            f = 1.0F;
         }

         f = friction / f;
         strafe *= f;
         forward *= f;
         float f1 = MathHelper.sin(data.getLocation().getYaw() * 3.1415927F / 180.0F);
         float f2 = MathHelper.cos(data.getLocation().getYaw() * 3.1415927F / 180.0F);
         float xAdd = strafe * f2 - forward * f1;
         float zAdd = forward * f2 + strafe * f1;
         return (double)MathUtil.hypot(xAdd, zAdd);
      } else {
         return 0.0;
      }
   }

   public static Pair moveFlyingPair(KarhuPlayer data, float strafe, float forward, boolean sprint) {
      float friction = (Float)moveEntityWithHeading(data, sprint).getY();
      float f = strafe * strafe + forward * forward;
      if (f >= 1.0E-4F) {
         f = MathHelper.sqrt_float(f);
         if (f < 1.0F) {
            f = 1.0F;
         }

         f = friction / f;
         strafe *= f;
         forward *= f;
         float yawRadius = data.getLocation().getYaw() * 3.1415927F / 180.0F;
         float f1 = MathHelper.sin(yawRadius);
         float f2 = MathHelper.cos(yawRadius);
         return new Pair(strafe * f2 - forward * f1, forward * f2 + strafe * f1);
      } else {
         return null;
      }
   }

   public static Pair moveFlyingPair2(KarhuPlayer data, float strafe, float forward, float friction) {
      float f = strafe * strafe + forward * forward;
      if (f >= 1.0E-4F) {
         f = MathHelper.sqrt_float(f);
         if (f < 1.0F) {
            f = 1.0F;
         }

         f = friction / f;
         strafe *= f;
         forward *= f;
         float yawRadius = data.getLocation().getYaw() * 3.1415927F / 180.0F;
         float f1 = MathHelper.sin(yawRadius);
         float f2 = MathHelper.cos(yawRadius);
         return new Pair(strafe * f2 - forward * f1, forward * f2 + strafe * f1);
      } else {
         return null;
      }
   }

   public static Pair moveEntityWithHeading(KarhuPlayer data) {
      float f1;
      float f2;
      float f3;
      if (!data.isOnWater()) {
         f1 = 0.91F;
         if (data.isLastOnGroundPacket()) {
            f1 = data.getCurrentFriction();
         }

         f3 = 0.16277136F / (f1 * f1 * f1);
         if (data.isLastOnGroundPacket()) {
            f2 = data.getWalkSpeed();
            f2 += f2 * 0.3F;
            f2 *= f3;
         } else {
            f2 = 0.025999999F;
         }

         return new Pair(f1, f2);
      } else {
         f1 = data.isSprinting() && data.isNewerThan12() ? 0.9F : 0.8F;
         f2 = 0.02F;
         if (data.getDepthStriderLevel() > 0) {
            f3 = (float)data.getDepthStriderLevel();
         } else {
            f3 = 0.0F;
         }

         if (f3 > 3.0F) {
            f3 = 3.0F;
         }

         if (!data.isLastOnGroundPacket()) {
            f3 *= 0.5F;
         }

         if (f3 > 0.0F) {
            f1 += (0.54600006F - f1) * f3 / 3.0F;
            f2 += (data.getWalkSpeed() * 1.0F - f2) * f3 / 3.0F;
         }

         if (data.getDolphinLevel() > 0) {
            f1 = 0.96F;
         }

         return new Pair(f1, f2);
      }
   }

   public static Pair moveEntityWithHeading(KarhuPlayer data, boolean sprint) {
      float f1;
      float f2;
      float f3;
      if (!data.isLastOnWaterOffset()) {
         f1 = 0.91F;
         if (data.isLastOnGroundPacket()) {
            f1 = data.getLastTickFriction();
         }

         f3 = 0.16277136F / (f1 * f1 * f1);
         if (data.isLastOnGroundPacket()) {
            f2 = data.getWalkSpeed();
            if (sprint) {
               f2 += f2 * 0.3F;
            }

            f2 *= f3;
         } else {
            f2 = sprint ? 0.025999999F : 0.02F;
         }

         return new Pair(f1, f2);
      } else {
         f1 = data.isSprinting() && data.isNewerThan12() ? 0.9F : 0.8F;
         f2 = 0.02F;
         if (data.getDepthStriderLevel() > 0) {
            f3 = (float)data.getDepthStriderLevel();
         } else {
            f3 = 0.0F;
         }

         if (f3 > 3.0F) {
            f3 = 3.0F;
         }

         if (!data.isLastOnGroundPacket()) {
            f3 *= 0.5F;
         }

         if (f3 > 0.0F) {
            f1 += (0.54600006F - f1) * f3 / 3.0F;
            f2 += (data.getWalkSpeed() * 1.0F - f2) * f3 / 3.0F;
         }

         if (data.getDolphinLevel() > 0) {
            f1 = 0.96F;
         }

         return new Pair(f1, f2);
      }
   }

   public static double loopKeys(KarhuPlayer data) {
      double maxSpeed = Double.MIN_VALUE;

      for(int strafe = 1; strafe >= -1; --strafe) {
         for(int forward = 1; forward >= -1; --forward) {
            float currentStrafe = (float)strafe * 0.98F;
            float currentForward = (float)forward * 0.98F;
            double moveFlying = moveFlying(data, currentStrafe, currentForward, true);
            if (moveFlying > maxSpeed) {
               maxSpeed = moveFlying;
            }
         }
      }

      return maxSpeed;
   }

   public static Pair loopKeysGetKeys(KarhuPlayer data, double kbX, double kbZ) {
      Map dataAssessments = new HashMap();
      double x = kbX;
      double z = kbZ;
      Iterator var10 = KEY_COMBOS.iterator();

      while(var10.hasNext()) {
         float[] floats = (float[])var10.next();
         boolean[] var12 = BOOLEANS;
         int var13 = var12.length;

         for(int var14 = 0; var14 < var13; ++var14) {
            boolean sprint = var12[var14];
            boolean[] var16 = BOOLEANS_REVERSED;
            int var17 = var16.length;

            for(int var18 = 0; var18 < var17; ++var18) {
               boolean blocking = var16[var18];
               boolean[] var20 = BOOLEANS_REVERSED;
               int var21 = var20.length;

               for(int var22 = 0; var22 < var21; ++var22) {
                  boolean jumped = var20[var22];
                  float strafe = floats[0];
                  float forward = floats[1];
                  if (jumped && sprint) {
                     float f = data.getLocation().getYaw() * 0.017453292F;
                     kbX -= (double)(MathHelper.sin(f) * 0.2F);
                     kbZ += (double)(MathHelper.cos(f) * 0.2F);
                  }

                  if (data.isWasSneaking()) {
                     strafe = (float)((double)strafe * 0.3);
                     forward = (float)((double)forward * 0.3);
                  }

                  if (blocking) {
                     strafe *= 0.2F;
                     forward *= 0.2F;
                  }

                  strafe *= 0.98F;
                  forward *= 0.98F;
                  Pair xzPair = moveFlyingPair(data, strafe, forward, sprint);
                  if (xzPair != null) {
                     kbX += (double)(Float)xzPair.getX();
                     kbZ += (double)(Float)xzPair.getY();
                  }

                  double deltaX = data.deltas.deltaX - kbX;
                  double deltaZ = data.deltas.deltaZ - kbZ;
                  double hypot = MathUtil.hypot(deltaX, deltaZ);
                  if (hypot <= 0.001) {
                     return new Pair(kbX, kbZ);
                  }

                  dataAssessments.put(hypot, new Pair(kbX, kbZ));
                  kbZ = z;
                  kbX = x;
               }
            }
         }
      }

      return (Pair)dataAssessments.get(dataAssessments.keySet().stream().mapToDouble((d) -> {
         return (double) d;
      }).min().orElse(3865386.0));
   }

   public static Pair bruteforceAttack(KarhuPlayer data, double kbX, double kbZ) {
      Map diffs = new HashMap();
      double original = MathUtil.hypot(data.deltas.deltaX - kbX, data.deltas.deltaZ - kbZ);
      diffs.put(original, new Pair(kbX, kbZ));
      double min = data.getClientVersion().getProtocolVersion() > 47 ? 0.003 : 0.005;
      kbX = Math.abs(kbX) < min ? 0.0 : kbX;
      kbZ = Math.abs(kbZ) < min ? 0.0 : kbZ;
      double ogX = kbX;
      double ogZ = kbZ;
      int j = 0;

      while(true) {
         ++j;
         if (j > data.getAttacks()) {
            Pair pair = (Pair)diffs.get(diffs.keySet().stream().mapToDouble((d) -> {
               return (double) d;
            }).min().orElse(0.0));
            diffs.clear();
            return pair;
         }

         ogX *= 0.6;
         ogZ *= 0.6;
         Pair dataX = loopKeysGetKeys(data, ogX, ogZ);
         double diffMult = MathUtil.hypot(data.deltas.deltaX - (Double)dataX.getX(), data.deltas.deltaZ - (Double)dataX.getY());
         if (diffMult <= 0.001) {
            return new Pair(dataX.getX(), dataX.getY());
         }

         diffs.put(diffMult, new Pair(dataX.getX(), dataX.getY()));
         ogX = kbX;
         ogZ = kbZ;
      }
   }
}
