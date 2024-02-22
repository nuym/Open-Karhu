package me.liwk.karhu.check.impl.combat.reach;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.impl.combat.hitbox.HitboxA;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.AttackEvent;
import me.liwk.karhu.event.Event;
import me.liwk.karhu.event.FlyingEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.mc.MovingObjectPosition;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.mc.vec.Vec3;
import org.bukkit.util.Vector;

@CheckInfo(
   name = "Reach (A)",
   category = Category.COMBAT,
   subCategory = SubCategory.REACH,
   experimental = false
)
public final class ReachA extends PacketCheck {
   public ReachA(KarhuPlayer data, Karhu karhu) {
      super(data, karhu);
   }

   public void handle(Event packet) {
      if (!Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         if (packet instanceof FlyingEvent) {
            this.handleReach(((FlyingEvent)packet).hasLooked());
         } else if (packet instanceof AttackEvent) {
            ClientVersion version = this.data.getClientVersion();
            if (!this.data.isPossiblyTeleporting()) {
               Iterator var3 = this.data.getLastTargets().iterator();

               while(var3.hasNext()) {
                  int target = (Integer)var3.next();
                  EntityData edata = (EntityData)this.data.getEntityData().get(target);
                  if (edata != null) {
                     if (this.data.isNewerThan8()) {
                        double dist = this.data.getBoundingBox().distanceToHitbox(edata.getEntityBoundingBox());
                        if (Math.abs(this.data.getLocation().pitch) != 90.0F) {
                           dist /= Math.cos(Math.toRadians((double)this.data.getLocation().pitch));
                        }

                        if (dist > 4.0) {
                           Karhu.getInstance().printCool("&b> &fKarhu USER: " + this.data.getBukkitPlayer().getName() + " is reaching far " + dist);
                           this.data.setEntityIdCancel(target);
                        }

                        this.data.setReduceNextDamage(dist > 4.0);
                     }
                  } else {
                     this.data.setReduceNextDamage(true);
                     this.data.setEntityIdCancel(target);
                  }
               }
            }

            if (!this.data.isForceCancelReach()) {
               int max = version.getProtocolVersion() <= 47 ? 2 : 21;
               this.data.setCancelTripleHit(this.data.getAttacks() > max);
            }
         }

      }
   }

   private void handleReach(boolean look) {
      Iterator var2 = this.data.getLastTargets().iterator();

      while(true) {
         while(true) {
            int target;
            do {
               do {
                  do {
                     do {
                        if (!var2.hasNext()) {
                           return;
                        }

                        target = (Integer)var2.next();
                     } while(this.data.getGameMode() == GameMode.CREATIVE);
                  } while(this.data.isRiding());
               } while(this.data.isSpectating());
            } while(this.data.isPossiblyTeleporting());

            if (this.data.getLastAttackTick() > 1) {
               return;
            }

            EntityData edata = (EntityData)this.data.getEntityData().get(target);
            if (edata == null) {
               this.data.setReachBypass(true);
               return;
            }

            if (edata.isRiding()) {
               this.data.setReachBypass(true);
               return;
            }

            float pitch = this.data.getLocation().pitch;
            float yaw = this.data.getLocation().yaw;
            List rotationVectors = new ArrayList();
            if (look) {
               rotationVectors.add(MathUtil.getVectorForRotation(pitch, yaw, this.data));
               if (this.data.getClientVersion().getProtocolVersion() >= 47) {
                  rotationVectors.add(MathUtil.getVectorForRotation(pitch, this.data.attackerYaw, this.data));
               }
            } else {
               rotationVectors.add(MathUtil.getVectorForRotation(this.data.attackerPitch, this.data.attackerYaw, this.data));
            }

            this.data.setReachBypass(false);
            AxisAlignedBB box = edata.getEntityBoundingBox();
            AxisAlignedBB axisalignedbb = !edata.uncertainBox ? MathUtil.getHitbox(this.data, box) : MathUtil.getHitbox(this.data, box).union(MathUtil.getHitbox(this.data, edata.getEntityBoundingBoxLast())).expand(0.1, 0.1, 0.1);
            double distance = Double.MAX_VALUE;
            Iterator var12 = rotationVectors.iterator();

            while(true) {
               while(var12.hasNext()) {
                  Vec3 rLook = (Vec3)var12.next();
                  Iterator var14 = this.data.getEyePositions().iterator();

                  while(var14.hasNext()) {
                     double height = (Double)var14.next();
                     Vec3 eyeLocation = new Vec3(this.data.attackerX, this.data.attackerY + height, this.data.attackerZ);
                     Vec3 search = eyeLocation.addVector(rLook.xCoord * 6.0, rLook.yCoord * 6.0, rLook.zCoord * 6.0);
                     MovingObjectPosition intercept = axisalignedbb.calculateIntercept(eyeLocation, search);
                     if (axisalignedbb.isVecInside(eyeLocation)) {
                        distance = 0.0;
                        break;
                     }

                     if (intercept != null) {
                        distance = Math.min(eyeLocation.distanceTo(intercept.hitVec), distance);
                        if (Karhu.getInstance().getAlertsManager().hasDebugToggled(this.data.getBukkitPlayer()) && distance > 3.0 && distance != Double.MAX_VALUE) {
                           this.data.getBukkitPlayer().sendMessage(this.format(4, distance));
                        }
                     }
                  }
               }

               double x = box.getCenterX();
               double z = box.getCenterZ();
               double direction = MathUtil.getDirection(this.data.getLocation(), new Vector(x, 0.0, z));
               double angle = MathUtil.getAngleDistance((double)this.data.getLocation().getYaw(), direction);
               double dist = this.data.getLastBoundingBox().distanceToHitbox(edata.getEntityBoundingBox());
               if (Math.abs(this.data.getLocation().pitch) != 90.0F) {
                  dist /= Math.cos(Math.toRadians((double)this.data.getLocation().pitch));
               }

               double buffer = Math.max(this.cfg.getReachBuffer(), 1.2);
               double removal = Math.max(this.cfg.getReachDecayPerMiss(), 0.001);
               double minReach = Math.max(this.cfg.getReachToFlag(), 3.0);
               boolean checkReach = true;
               if (distance == Double.MAX_VALUE) {
                  if (Karhu.getInstance().getConfigManager().isCheckHitbox()) {
                     if (++this.subVl > (double)((angle > 20.0 ? 2 : 4) + (dist > 8.0 ? 1 : 2))) {
                        ((HitboxA)this.data.getCheckManager().getCheck(HitboxA.class)).fail("* Hit out of the box\n * dist §b" + dist + "\n * angle §b" + angle + "\n * mins §b" + edata.minX + " / " + edata.minY + " / " + edata.minZ + "\n * maxs §b" + edata.maxX + " / " + edata.maxY + " / " + edata.maxZ + "\n * locations §b" + edata.newLocations.size() + "\n * existed §b" + edata.getExist() + "\n §f* DEV DATA: §b" + edata.posIncrements, 300L);
                        this.data.setCancelNextHitH(true);
                     } else {
                        ((HitboxA)this.data.getCheckManager().getCheck(HitboxA.class)).debug(String.format("A: %.3f D: %.3f B: %.3f", angle, dist, this.subVl));
                     }
                  }

                  checkReach = false;
               } else {
                  this.subVl = Math.max(this.subVl - 0.175, 0.0);
                  this.data.setCancelNextHitH(false);
               }

               if (distance > this.data.getHighestReach()) {
                  this.data.setHighestReach(distance);
               }

               if (distance >= minReach && checkReach) {
                  this.violations += distance - minReach + 0.4;
                  if (this.violations >= buffer) {
                     this.fail("§f* Longer arms\n §f* Range: §b" + distance + "\n §f* DEV DATA: §b" + edata.posIncrements + "\n §f* existed: §b" + edata.getExist() + "\n §f* locations: §b" + edata.newLocations.size() + " | " + edata.isUncertainBox() + "\n §f* DEV DATA: §b" + this.data.getTeleportManager().zeroAmount + "/" + this.data.getTeleportManager().teleportAmount, this.getBanVL(), 300L);
                     this.data.setCancelNextHitR(true);
                  }
                  break;
               }

               if (checkReach) {
                  this.decrease(removal);
                  this.data.setCancelNextHitR(false);
               }
               break;
            }
         }
      }
   }
}
