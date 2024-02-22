package me.liwk.karhu.check.impl.combat.velocity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.pair.Pair;
import me.liwk.karhu.util.pair.Pair3;
import me.liwk.karhu.util.pair.VelocityData5;
import me.liwk.karhu.world.nms.NMSValueParser;
import org.bukkit.util.Vector;

@CheckInfo(
   name = "Velocity (B)",
   category = Category.COMBAT,
   subCategory = SubCategory.VELOCITY,
   experimental = false
)
public final class VelocityB extends PacketCheck {
   private double kbZ;
   private double kbX;
   private double allowance;
   private int ticks;
   private int attacks;
   private int bruteforcedAttacks;
   private boolean onGround;
   private boolean attack;
   private boolean jump;

   public VelocityB(KarhuPlayer data, Karhu karhu) {
      super(data, karhu);
   }

   public void handle(Event packet) {
      if (packet instanceof FlyingEvent) {
         Vector tickVel = this.data.getTickedVelocity();
         if (tickVel != null) {
            this.kbX = tickVel.getX();
            this.kbZ = tickVel.getZ();
            this.allowance = 0.001;
            if (this.data.getMoveTicks() <= 1) {
               this.allowance = this.data.offsetMove() + 0.001;
            }
         }

         if (this.canCheckCondition()) {
            if (this.attack) {
               this.bruteforceAttack();
            }

            float f4 = 0.91F;
            if (this.onGround) {
               f4 = this.data.getCurrentFriction();
            }

            this.kbX = Math.abs(this.kbX) < this.data.clamp() ? 0.0 : this.kbX;
            this.kbZ = Math.abs(this.kbZ) < this.data.clamp() ? 0.0 : this.kbZ;
            if (!this.data.isInWeb() && !this.data.isGliding() && !this.data.isRiding() && this.data.elapsed(this.data.getLastSneakEdge()) > 5 && !this.data.isPossiblyTeleporting() && this.data.elapsed(this.data.getLastOnClimbable()) > 5 && this.data.elapsed(this.data.getLastCollidedWithEntity()) > 8 && this.data.elapsed(this.data.getLastInLiquid()) > 5 && this.data.elapsed(this.data.getLastOnBoat()) > 1 && this.data.elapsed(this.data.getLastCollided()) > 1 && this.data.elapsed(this.data.getLastCollidedGhost()) > 1) {
               VelocityData5 data = this.computeKeys(this.kbX, this.kbZ);
               if (data == null) {
                  this.resetState();
               } else {
                  double dClientKb = this.data.deltas.deltaXZ;
                  float strafe = (Float)data.getA();
                  float forward = (Float)data.getX();
                  float friction = (Float)data.getY();
                  boolean fastMath = (Boolean)data.getO();
                  boolean thinkJump = (Boolean)data.getP();
                  if (thinkJump) {
                     float radians = this.data.getLocation().getYaw() * 0.017453292F;
                     this.kbX -= (double)(MathHelper.sin(radians) * 0.2F);
                     this.kbZ += (double)(MathHelper.cos(radians) * 0.2F);
                  }

                  this.moveFlying(strafe, forward, friction, fastMath);
                  double dKbZ = this.data.deltas.deltaX / this.kbX;
                  double dKbX = this.data.deltas.deltaZ / this.kbZ;
                  double dKb = MathUtil.hypot(this.kbX, this.kbZ);
                  double diff = dKb - dClientKb;
                  double p = dClientKb / dKb * 100.0;
                  double minPtc = 99.99;
                  minPtc -= this.data.getBukkitPlayer().getMaximumNoDamageTicks() < 10 ? 20.0 : 0.0;
                  minPtc -= this.data.isNewerThan8() ? 20.0 : 0.0;
                  double maxVL = this.data.isNewerThan8() ? 7.0 : 4.0;
                  boolean reversed = dKbZ < -0.05 || dKbX < -0.05;
                  if (thinkJump) {
                     float radians = this.data.getLocation().getYaw() * 0.017453292F;
                     this.kbX -= (double)(MathHelper.sin(radians) * 0.2F);
                     this.kbZ += (double)(MathHelper.cos(radians) * 0.2F);
                  }

                  if (p < minPtc && Math.abs(diff) > this.allowance || reversed && !this.data.isJumped()) {
                     this.violations = Math.min(15.0, this.violations + Math.abs(1.975 - Math.abs(dClientKb / dKb)));
                     if (this.violations > maxVL) {
                        this.fail("* Horizontal Modification\n §f* approx pct: §b" + this.format(3, p) + "\n §f* client: §b" + this.format(3, dClientKb) + "\n §f* server: §b" + this.format(3, dKb) + "\n §f* jump: §b" + this.data.isJumped() + " | " + thinkJump + "\n §f* tick: §b" + this.ticks + " | " + this.data.getMoveTicks() + "\n §f* attack: §b" + this.attack + " | " + this.data.getLastAttackTick() + " | " + this.attacks + "\n §f* st/fo/fr: §b" + strafe + " | " + forward + " | " + friction + "\n §f* version: §b" + MathUtil.parseVersion(this.data.getClientVersion()) + "\n §f* reverse: §b" + reversed + " | " + this.format(3, dKbX) + " | " + this.format(3, dKbZ), this.getBanVL(), 60L);
                     }

                     this.debug(String.format("PTC: %.3f, D: %.6f, T: %d, A: %b, R: %b, B: %.2f", p, diff, this.ticks, this.attack, reversed, this.violations));
                     this.resetState();
                  } else {
                     this.violations = Math.max(this.violations - 0.065, 0.0);
                  }

                  this.kbX *= (double)f4;
                  this.kbZ *= (double)f4;
                  if (this.ticks++ >= 8 || this.kbZ == 0.0 && this.kbX == 0.0) {
                     this.resetState();
                  }
               }
            } else {
               this.resetState();
            }
         } else {
            this.resetState();
         }

         this.onGround = ((FlyingEvent)packet).isOnGround();
         this.attack = false;
         this.attacks = 0;
         this.bruteforcedAttacks = 0;
      } else if (packet instanceof AttackEvent && ((AttackEvent)packet).isPlayer()) {
         this.attack = true;
         ++this.attacks;
      }

   }

   private void resetState() {
      this.kbX = 0.0;
      this.kbZ = 0.0;
      this.ticks = 0;
   }

   private boolean canCheckCondition() {
      return this.kbX * this.kbX + this.kbZ * this.kbZ > this.data.offsetMove() + 0.001 && this.data.elapsed(this.data.getLastFlyTick()) > 30;
   }

   private VelocityData5 computeKeys(double x, double z) {
      Map dataAssessments = new HashMap();
      Iterator var6 = NMSValueParser.KEY_COMBOS.iterator();

      while(var6.hasNext()) {
         float[] floats = (float[])var6.next();
         boolean[] var8 = BOOLEANS;
         int var9 = var8.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            boolean using = var8[var10];
            boolean[] var12 = BOOLEANS;
            int var13 = var12.length;

            for(int var14 = 0; var14 < var13; ++var14) {
               boolean sprinting = var12[var14];
               boolean[] var16 = BOOLEANS_REVERSED;
               int var17 = var16.length;

               for(int var18 = 0; var18 < var17; ++var18) {
                  boolean sneaking = var16[var18];
                  boolean[] var20 = BOOLEANS_REVERSED;
                  int var21 = var20.length;

                  for(int var22 = 0; var22 < var21; ++var22) {
                     boolean jump = var20[var22];
                     float strafe = floats[0];
                     float forward = floats[1];
                     float friction = (Float)this.moveEntityWithHeading(sprinting).getY();
                     if (sneaking) {
                        strafe = (float)((double)strafe * 0.3);
                        forward = (float)((double)forward * 0.3);
                     }

                     if (using) {
                        strafe *= 0.2F;
                        forward *= 0.2F;
                     }

                     boolean didJump = false;
                     if (jump && sprinting && this.onGround) {
                        float radians = this.data.getLocation().getYaw() * 0.017453292F;
                        this.kbX -= (double)(MathHelper.sin(radians) * 0.2F);
                        this.kbZ += (double)(MathHelper.cos(radians) * 0.2F);
                        didJump = true;
                     }

                     strafe *= 0.98F;
                     forward *= 0.98F;
                     this.moveFlying(strafe, forward, friction, false);
                     double deltaX = this.data.deltas.deltaX - this.kbX;
                     double deltaZ = this.data.deltas.deltaZ - this.kbZ;
                     double offsetH = MathUtil.hypot(deltaX, deltaZ);
                     dataAssessments.put(offsetH, new VelocityData5(strafe, forward, friction, false, didJump));
                     this.kbX = x;
                     this.kbZ = z;
                  }
               }
            }
         }
      }

      double closest = dataAssessments.keySet().stream().mapToDouble((d) -> {
         return d;
      }).min().orElse(3865386.0);
      VelocityData5 result = (VelocityData5)dataAssessments.get(closest);
      dataAssessments.clear();
      return result;
   }

   private void moveFlying(float strafe, float forward, float friction, boolean fastMath) {
      float f = strafe * strafe + forward * forward;
      if (f >= 1.0E-4F) {
         f = MathHelper.sqrt_float(f);
         if (f < 1.0F) {
            f = 1.0F;
         }

         f = friction / f;
         strafe *= f;
         forward *= f;
         float yawRadius = this.data.getLocation().getYaw() * 3.1415927F / 180.0F;
         float f1 = MathHelper.sin(fastMath, yawRadius);
         float f2 = MathHelper.cos(fastMath, yawRadius);
         this.kbX += (double)(strafe * f2 - forward * f1);
         this.kbZ += (double)(forward * f2 + strafe * f1);
      }

   }

   private Pair moveEntityWithHeading(boolean sprint) {
      float f4 = 0.91F;
      float f5 = this.data.getWalkSpeed();
      if (this.onGround) {
         f4 = this.data.getCurrentFriction();
         float f = 0.16277136F / (f4 * f4 * f4);
         if (sprint) {
            f5 += f5 * 0.3F;
         }

         f5 *= f;
      } else {
         f5 = sprint ? 0.025999999F : 0.02F;
      }

      return new Pair(f4, f5);
   }

   private void bruteforceAttack() {
      Map diffs = new HashMap();
      double ogX = this.kbX;
      double ogZ = this.kbZ;
      double original = MathUtil.hypot(this.data.deltas.deltaX - this.kbX, this.data.deltas.deltaZ - this.kbZ);
      diffs.put(original, new Pair3(this.kbX, this.kbZ, 0));
      int j = 0;

      while(true) {
         ++j;
         if (j > this.attacks) {
            Pair3 pair = (Pair3)diffs.get(diffs.keySet().stream().mapToDouble((d) -> {
               return d;
            }).min().orElse(-420.0));
            if (pair != null) {
               this.kbX = (Double)pair.getX();
               this.kbZ = (Double)pair.getY();
               this.bruteforcedAttacks = (Integer)pair.getZ();
            }

            diffs.clear();
            return;
         }

         ogX *= 0.6;
         ogZ *= 0.6;
         double unMovedOgX = ogX;
         double unMovedOgZ = ogZ;
         VelocityData5 data = this.computeKeys(ogX, ogZ);
         if (data != null) {
            float strafe = (Float)data.getA();
            float forward = (Float)data.getX();
            float friction = (Float)data.getY();
            Pair directionAdd = NMSValueParser.moveFlyingPair2(this.data, strafe, forward, friction);
            if (directionAdd != null) {
               ogX += (double)(Float)directionAdd.getX();
               ogZ += (double)(Float)directionAdd.getY();
            }
         }

         double diffMult = MathUtil.hypot(this.data.deltas.deltaX - ogX, this.data.deltas.deltaZ - ogZ);
         diffs.put(diffMult, new Pair3(unMovedOgX, unMovedOgZ, j));
         ogX = unMovedOgX;
         ogZ = unMovedOgZ;
      }
   }
}
