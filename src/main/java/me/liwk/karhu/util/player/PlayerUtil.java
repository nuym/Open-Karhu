package me.liwk.karhu.util.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.player.PlayerUtil.1;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public final class PlayerUtil {
   private static final UUID SPRINTING_SPEED_BOOST = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
   public static final String legacyMovementSpeed = "generic.movementSpeed";
   public static final String movementSpeed = "minecraft:generic.movement";

   public static float getScaledFriction(KarhuPlayer data) {
      float f4 = data.getCurrentFriction();
      float f = 0.16277136F / (f4 * f4 * f4);
      float f5;
      if (data.isLastOnGroundPacket()) {
         f5 = data.getAttributeSpeed() * f;
      } else {
         f5 = data.isWasWasSprinting() ? 0.025999999F : 0.02F;
      }

      return f5;
   }

   public static float getScaledFriction(KarhuPlayer data, boolean sprinting) {
      float f4 = data.getCurrentFriction();
      float f = 0.16277136F / (f4 * f4 * f4);
      float f5;
      if (data.isLastOnGroundPacket()) {
         f5 = data.getAttributeSpeed() * f;
      } else {
         f5 = sprinting ? 0.025999999F : 0.02F;
      }

      return f5;
   }

   public static double getMovementSpeed(List collection, double base) {
      WrapperPlayServerUpdateAttributes.PropertyModifier modifier;
      for(Iterator var3 = getModifiers(PlayerUtil.Operation.ADDITION, collection).iterator(); var3.hasNext(); base += modifier.getAmount()) {
         modifier = (WrapperPlayServerUpdateAttributes.PropertyModifier)var3.next();
      }

      double moveSpeed = base;

      Iterator var5;
      WrapperPlayServerUpdateAttributes.PropertyModifier modifier;
      for(var5 = getModifiers(PlayerUtil.Operation.MULTIPLY_BASE, collection).iterator(); var5.hasNext(); moveSpeed += base * modifier.getAmount()) {
         modifier = (WrapperPlayServerUpdateAttributes.PropertyModifier)var5.next();
      }

      for(var5 = getModifiers(PlayerUtil.Operation.MULTIPLY_TOTAL, collection).iterator(); var5.hasNext(); moveSpeed *= 1.0 + modifier.getAmount()) {
         modifier = (WrapperPlayServerUpdateAttributes.PropertyModifier)var5.next();
      }

      return moveSpeed;
   }

   private static List getModifiers(Operation operation, List modifiers) {
      List results = new ArrayList();
      Iterator var3 = modifiers.iterator();

      while(var3.hasNext()) {
         WrapperPlayServerUpdateAttributes.PropertyModifier modifier = (WrapperPlayServerUpdateAttributes.PropertyModifier)var3.next();
         if (!modifier.getUUID().equals(SPRINTING_SPEED_BOOST) && getOperation(modifier.getOperation()) == operation) {
            results.add(modifier);
         }
      }

      return results;
   }

   private static Operation getOperation(WrapperPlayServerUpdateAttributes.PropertyModifier.Operation operation) {
      switch (1.$SwitchMap$com$github$retrooper$packetevents$wrapper$play$server$WrapperPlayServerUpdateAttributes$PropertyModifier$Operation[operation.ordinal()]) {
         case 1:
            return PlayerUtil.Operation.ADDITION;
         case 2:
            return PlayerUtil.Operation.MULTIPLY_BASE;
         case 3:
            return PlayerUtil.Operation.MULTIPLY_TOTAL;
         default:
            return null;
      }
   }

   public static float getJumpHeight(KarhuPlayer data, float base) {
      return base + getJumpBooster(data);
   }

   public static float getJumpHeight(KarhuPlayer data) {
      return Math.max(0.0F, 0.42F * data.getJumpFactor() + getJumpBooster(data, false));
   }

   public static float getJumpBooster(KarhuPlayer data) {
      return (float)getJumpBoostLevel(data) * 0.1F;
   }

   public static float getJumpBooster(KarhuPlayer data, boolean maxed) {
      return (float)getJumpBoostLevel(data, maxed) * 0.1F;
   }

   public static int getJumpBoostLevel(KarhuPlayer data) {
      return Math.max(0, Math.max(data.getJumpBoost(), data.getCacheBoost()));
   }

   public static int getJumpBoostLevel(KarhuPlayer data, boolean maxed) {
      return maxed ? Math.max(0, Math.max(data.getJumpBoost(), data.getCacheBoost())) : Math.max(data.getJumpBoost(), data.getCacheBoost());
   }

   public static float getBaseSpeedAttribute(KarhuPlayer data, float mult) {
      return data.getWalkSpeed() * mult;
   }

   public static float getBaseSpeedAirAttribute(KarhuPlayer data, float multi) {
      return 0.35F + data.getAttributeSpeed() * multi;
   }

   public static void sendPacket(Player player, PacketWrapper wrapper) {
      PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
   }

   public static void sendPacket(Player player, short id) {
      if (Karhu.PING_PONG_MODE) {
         WrapperPlayServerPing ping = new WrapperPlayServerPing(id);
         PacketEvents.getAPI().getPlayerManager().sendPacket(player, ping);
      } else {
         WrapperPlayServerWindowConfirmation transaction = new WrapperPlayServerWindowConfirmation(0, id, false);
         PacketEvents.getAPI().getPlayerManager().sendPacket(player, transaction);
      }

   }

   public static void sendPacket(Object channel, PacketWrapper wrapper) {
      PacketEvents.getAPI().getPlayerManager().sendPacket(channel, wrapper);
   }

   public static boolean isGeyserPlayer(Player player) {
      return !Karhu.getInstance().isFloodgate() ? false : FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
   }

   public static boolean isGeyserPlayer(UUID uuid) {
      return !Karhu.getInstance().isFloodgate() ? false : FloodgateApi.getInstance().isFloodgatePlayer(uuid);
   }

   public static enum Operation {
      ADDITION,
      MULTIPLY_BASE,
      MULTIPLY_TOTAL;
   }
}
