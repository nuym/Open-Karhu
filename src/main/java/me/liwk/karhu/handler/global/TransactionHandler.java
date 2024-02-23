package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUseBed;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState.Reason;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation.EntityAnimationType;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.RespawnEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.Teleport;
import me.liwk.karhu.util.TeleportPosition;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.pending.VelocityPending;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.task.Tasker;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TransactionHandler {
   public void handlePlayReceive(PacketPlayReceiveEvent e, long nanoTime, KarhuPlayer data) {
      PacketType.Play.Client type = e.getPacketType();
      long now = e.getTimestamp();
      switch (type) {
         case PONG:
         case WINDOW_CONFIRMATION:
            short number;
            if (type == Client.PONG) {
               number = (short)(new WrapperPlayClientPong(e)).getId();
            } else {
               number = (new WrapperPlayClientWindowConfirmation(e)).getActionId();
            }

            data.setLastLastClientTransaction(data.getLastClientTransaction());
            data.setLastClientTransaction(data.getCurrentClientTransaction());
            if (number <= -3000) {
               data.setCurrentClientTransaction(number);
            }

            if (data.getScheduledTransactions().containsKey(number)) {
               ((Consumer)data.getScheduledTransactions().remove(number)).accept(number);
               ++data.receivedConfirms;
            }

            ObjectArrayList packetList = (ObjectArrayList)data.getWaitingConfirms().get(number);
            if (packetList != null && !packetList.isEmpty()) {
               ObjectListIterator var10 = packetList.iterator();

               while(var10.hasNext()) {
                  Consumer consumer = (Consumer)var10.next();
                  consumer.accept(number);
               }

               packetList.clear();
               data.getWaitingConfirms().remove(number);
            }

            Deque slots = (Deque)data.getBackSwitchSlots().get(Integer.valueOf(number));
            if (data.isPendingBackSwitch() && slots != null) {
               data.setPendingBackSwitch(false);
               if (slots.peekFirst() != null) {
                  int slot = (Integer)slots.peekFirst();
                  PlayerUtil.sendPacket((Player)data.getBukkitPlayer(), new WrapperPlayServerHeldItemChange(slot));
               }

               slots.remove(Integer.valueOf(number));
            }

            if (data.getTransactionTime().containsKey(number)) {
               long transactionStamp = (Long)data.getTransactionTime().get(number);
               if (!data.isHasReceivedTransaction()) {
                  data.setTransactionClock(transactionStamp);
               }

               data.setHasReceivedTransaction(true);
               data.setLastTransactionPing(data.getTransactionPing());
               data.setTransactionPing(TimeUnit.NANOSECONDS.toMillis(nanoTime - transactionStamp));
               data.getTransactionTime().remove(number);
               data.setLastTransactionPingUpdate(transactionStamp);
               data.setPingInTicks(Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L)));
            }

            data.getNetHandler().handleClientTransaction(number);
            data.setLastTransaction(now);
         default:
      }
   }

   public void handlePacketPlaySend(PacketPlaySendEvent e, long nanoTime, KarhuPlayer data) {
      PacketType.Play.Server type = e.getPacketType();
      if (data.isObjectLoaded()) {
         int vehicleId;
         boolean riding;
         double deltaZ;
         int effectId;
         double deltaY;
         Vector3d pos;
         double deltaX;
         switch (type) {
            case PLAYER_POSITION_AND_LOOK:
               WrapperPlayServerPlayerPositionAndLook position = new WrapperPlayServerPlayerPositionAndLook(e);
               pos = new Vector3d(position.getX(), position.getY(), position.getZ());
               CustomLocation locationPlayer = data.getLocation();
               if (!data.isNewerThan8()) {
                  if (position.isRelativeFlag(RelativeFlag.X)) {
                     pos = pos.add(new Vector3d(locationPlayer.x, 0.0, 0.0));
                  }

                  if (position.isRelativeFlag(RelativeFlag.Y)) {
                     pos = pos.add(new Vector3d(0.0, locationPlayer.y, 0.0));
                  }

                  if (position.isRelativeFlag(RelativeFlag.Z)) {
                     pos = pos.add(new Vector3d(0.0, 0.0, locationPlayer.z));
                  }

                  position.setX(pos.getX());
                  position.setY(pos.getY());
                  position.setZ(pos.getZ());
                  position.setRelativeMask((byte)0);
               }

               riding = Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_7_10);
               double x = pos.getX();
               double y = !riding ? pos.getY() : pos.getY() - 1.6200000047683716;
               double z = pos.getZ();
               Teleport teleport = new Teleport(new TeleportPosition(x, y, z));
               data.queueToPrePing((uid) -> {
                  data.getTeleportManager().locations.add(teleport);
                  data.setInventoryOpen(false);
               });
               data.queueToPostPing((uid) -> {
                  data.queueToFlying(1, (tick) -> {
                     data.getTeleportManager().locations.remove(teleport);
                     --data.getTeleportManager().teleportsPending;
                  });
               });
               data.setLastTeleportPacket(data.getServerTick());
               break;
            case ENTITY_VELOCITY:
               WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(e);
               pos = velocity.getVelocity();
               if (velocity.getEntityId() == e.getUser().getEntityId()) {
                  effectId = data.getCurrentServerTransaction();
                  data.setConfirmingVelocity(true);
                  data.queueToPrePing((uid) -> {
                     int uidInt = uid;
                     if (uidInt != effectId) {
                        Karhu.getInstance().printCool("&b> &fDEBUG: " + uidInt + "/" + effectId);
                     }

                     Vector vector = new Vector(pos.getX(), pos.getY(), pos.getZ());
                     ConcurrentLinkedDeque velocities = (ConcurrentLinkedDeque)data.getVelocityPending().getOrDefault(uidInt, new ConcurrentLinkedDeque());
                     velocities.add(new VelocityPending((short)effectId, vector, false));
                     data.getVelocityPending().put(uidInt, velocities);
                  });
                  data.queueToPostPing((uid) -> {
                     ConcurrentLinkedDeque velos = data.getTickVelocities(effectId);
                     if (velos != null) {
                        Iterator var4 = velos.iterator();

                        while(var4.hasNext()) {
                           VelocityPending velocityPending = (VelocityPending)var4.next();
                           if (!velocityPending.isMarkedSent()) {
                              data.velocityTick(velocityPending.getVelocity());
                           }

                           velos.remove(velocityPending);
                           if (velos.isEmpty()) {
                              data.getVelocityPending().remove(effectId);
                           }
                        }
                     }

                  });
                  data.setPlayerVelocityCalled(false);
                  data.setPlayerExplodeCalled(false);
               }
               break;
            case EXPLOSION:
               WrapperPlayServerExplosion explosion = new WrapperPlayServerExplosion(e);
               Vector3f velocity = explosion.getPlayerMotion();
               if (velocity.getX() == 0.0F && velocity.getY() == 0.0F && velocity.getZ() == 0.0F) {
                  return;
               }

               data.queueToPrePing((uid) -> {
                  data.setLastVelocityTaken(data.getTotalTicks());
                  data.setVelocityXZTicks(0);
                  data.setVelocityYTicks(0);
                  data.setVelocityX((double)velocity.getX());
                  data.setVelocityY((double)velocity.getY());
                  data.setVelocityZ((double)velocity.getZ());
                  int velocityH = (int)Math.ceil((double)(Math.abs(velocity.getX()) + Math.abs(velocity.getZ())) / 2.0 + 2.0) * 4;
                  int velocityV = (int)Math.ceil(FastMath.pow((double)(Math.abs(velocity.getY()) + 2.0F), 2)) * 2;
                  data.setMaxVelocityXZTicks(velocityH + velocityV + 5);
                  data.setMaxVelocityYTicks(velocityV);
                  data.setTakingVertical(true);
                  data.setNeedExplosionAdditions(true);
                  data.setVelocityHorizontal((double)MathUtil.hypot(velocity.getX(), velocity.getZ()));
                  data.setConfirmingVelocity(true);
               });
               data.queueToPostPing((uid) -> {
                  data.setConfirmingVelocity(false);
               });
               break;
            case ENTITY_ROTATION:
               WrapperPlayServerEntityRotation look = new WrapperPlayServerEntityRotation(e);
               data.queueToPrePing((uid) -> {
                  EntityLocationHandler.updateEntityLook(data, look.getEntityId());
               });
               break;
            case ENTITY_RELATIVE_MOVE_AND_ROTATION:
            case ENTITY_RELATIVE_MOVE:
               int entityId;
               if (type == Server.ENTITY_RELATIVE_MOVE) {
                  WrapperPlayServerEntityRelativeMove entity = new WrapperPlayServerEntityRelativeMove(e);
                  entityId = entity.getEntityId();
                  deltaX = entity.getDeltaX();
                  deltaY = entity.getDeltaY();
                  deltaZ = entity.getDeltaZ();
               } else {
                  WrapperPlayServerEntityRelativeMoveAndRotation entity = new WrapperPlayServerEntityRelativeMoveAndRotation(e);
                  entityId = entity.getEntityId();
                  deltaX = entity.getDeltaX();
                  deltaY = entity.getDeltaY();
                  deltaZ = entity.getDeltaZ();
               }

               if (SpigotReflectionUtil.getEntityById(entityId) instanceof Player) {
                  data.queueToPrePing((uid) -> {
                     EntityLocationHandler.updateEntityRelMove2(data, entityId, deltaX, deltaY, deltaZ);
                  });
                  data.queueToPostPing((uid) -> {
                     EntityData edata = (EntityData)data.getEntityData().get(entityId);
                     if (edata != null) {
                        edata.postTransaction();
                     }

                  });
               }
               break;
            case SPAWN_LIVING_ENTITY:
               WrapperPlayServerSpawnLivingEntity wrapper = new WrapperPlayServerSpawnLivingEntity(e);
               deltaX = wrapper.getPosition().getX();
               deltaY = wrapper.getPosition().getY();
               deltaZ = wrapper.getPosition().getZ();
               data.queueToPrePing((uid) -> {
                  EntityLocationHandler.addEntity(data, wrapper.getEntityType(), deltaX, deltaY, deltaZ, wrapper.getEntityId());
               });
               break;
            case SPAWN_ENTITY:
               WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(e);
               deltaX = wrapper.getPosition().getX();
               deltaY = wrapper.getPosition().getY();
               deltaZ = wrapper.getPosition().getZ();
               data.queueToPrePing((uid) -> {
                  EntityLocationHandler.addEntity(data, wrapper.getEntityType(), deltaX, deltaY, deltaZ, wrapper.getEntityId());
               });
               break;
            case SPAWN_PLAYER:
               WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(e);
               deltaX = wrapper.getPosition().getX();
               deltaY = wrapper.getPosition().getY();
               deltaZ = wrapper.getPosition().getZ();
               data.queueToPrePing((uid) -> {
                  EntityLocationHandler.addEntity(data, EntityTypes.PLAYER, deltaX, deltaY, deltaZ, wrapper.getEntityId());
               });
               break;
            case ENTITY_TELEPORT:
               WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(e);
               pos = teleport.getPosition();
               effectId = teleport.getEntityId();
               data.queueToPrePing((uid) -> {
                  EntityData edata = (EntityData)data.getEntityData().get(effectId);
                  if (edata != null) {
                     if (Math.abs(edata.newX - pos.getX()) < 0.03125 && Math.abs(edata.newY - pos.getY()) < 0.015625 && Math.abs(edata.newZ - pos.getZ()) < 0.03125) {
                        EntityLocationHandler.updateEntityTeleport2(data, effectId, edata.newX, edata.newY, edata.newZ);
                     } else {
                        EntityLocationHandler.updateEntityTeleport2(data, effectId, pos.getX(), pos.getY(), pos.getZ());
                     }

                  }
               });
               data.queueToPostPing((uid) -> {
                  EntityData edata = (EntityData)data.getEntityData().get(effectId);
                  if (edata != null) {
                     edata.postTransaction();
                  }

               });
               break;
            case DESTROY_ENTITIES:
               WrapperPlayServerDestroyEntities wrapper = new WrapperPlayServerDestroyEntities(e);
               int[] entityIds = wrapper.getEntityIds();
               data.queueToPrePing((uid) -> {
                  EntityLocationHandler.destroyEntity(data, entityIds);
               });
               break;
            case USE_BED:
               WrapperPlayServerUseBed bed = new WrapperPlayServerUseBed(e);
               if (data.getEntityId() == bed.getEntityId()) {
                  data.queueToPrePing((uid) -> {
                     data.setInBed(true);
                     data.setBedPos(new Vec3((double)bed.getPosition().getX() + 0.5, (double)bed.getPosition().getY(), (double)bed.getPosition().getZ() + 0.5));
                  });
               }
               break;
            case ENTITY_ANIMATION:
               WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(e);
               if (data.getEntityId() == animation.getEntityId() && animation.getType() == EntityAnimationType.WAKE_UP) {
                  data.queueToPrePing((uid) -> {
                     data.setInBed(false);
                  });
               }
               break;
            case OPEN_WINDOW:
               data.queueToPrePing((uid) -> {
                  data.setInventoryOpen(true);
                  data.setInvStamp(data.getTotalTicks());
                  data.setUsingItem(false);
                  data.setEating(false);
               });
               break;
            case CLOSE_WINDOW:
               data.queueToPrePing((uid) -> {
                  data.setInventoryOpen(false);
                  data.setInvStamp(data.getTotalTicks());
               });
               break;
            case ENTITY_EFFECT:
               WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(e);
               vehicleId = wrapper.getEntityId();
               if (data.getEntityId() != vehicleId) {
                  return;
               }

               effectId = wrapper.getPotionType().getId(data.getClientVersion());
               int amplifier = wrapper.getEffectAmplifier();
               data.queueToPrePing((uid) -> {
                  data.getEffectManager().addPotionEffect(effectId, amplifier);
               });
               break;
            case REMOVE_ENTITY_EFFECT:
               WrapperPlayServerRemoveEntityEffect wrapper = new WrapperPlayServerRemoveEntityEffect(e);
               if (data.getEntityId() != wrapper.getEntityId()) {
                  return;
               }

               data.queueToPrePing((uid) -> {
                  data.getEffectManager().removePotionEffect(wrapper.getPotionType().getId(data.getClientVersion()));
               });
               break;
            case UPDATE_ATTRIBUTES:
               WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(e);
               if (packet.getEntityId() != data.getEntityId()) {
                  return;
               }

               data.queueToPrePing((task) -> {
                  Iterator var3 = packet.getProperties().iterator();

                  while(true) {
                     WrapperPlayServerUpdateAttributes.Property property;
                     do {
                        if (!var3.hasNext()) {
                           return;
                        }

                        property = (WrapperPlayServerUpdateAttributes.Property)var3.next();
                     } while(!property.getKey().startsWith("minecraft:generic.movement") && !property.getKey().startsWith("generic.movementSpeed"));

                     data.setWalkSpeed((float)PlayerUtil.getMovementSpeed(property.getModifiers(), property.getValue()));
                  }
               });
               break;
            case ENTITY_METADATA:
               WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(e);
               if (packet.getEntityId() == data.getEntityId() && Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_14)) {
                  int id = 12;
                  if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                     id = 13;
                  } else if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_17)) {
                     id = 14;
                  }

                  com.github.retrooper.packetevents.protocol.entity.data.EntityData bedBlock = getIndex(packet.getEntityMetadata(), id);
                  if (bedBlock != null) {
                     data.queueToPrePing((task) -> {
                        Optional bed = (Optional)bedBlock.getValue();
                        if (bed.isPresent()) {
                           Vector3i bedPos = (Vector3i)bed.get();
                           data.setInBed(true);
                           data.setBedPos(new Vec3((double)bedPos.getX() + 0.5, (double)bedPos.getY(), (double)bedPos.getZ() + 0.5));
                        } else {
                           data.setInBed(false);
                        }

                     });
                  }
               }
               break;
            case JOIN_GAME:
               WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(e);
               if (packet.getGameMode() != null) {
                  data.gameMode = packet.getGameMode();
                  data.setSpectating(data.getGameMode() == GameMode.SPECTATOR);
               }

               data.setEntityId(packet.getEntityId());
               break;
            case CHANGE_GAME_STATE:
               WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(e);
               if (packet.getReason() == Reason.CHANGE_GAME_MODE) {
                  data.queueToPrePing((uid) -> {
                     data.gameMode = GameMode.values()[(int)packet.getValue()];
                     data.setSpectating(data.getGameMode() == GameMode.SPECTATOR);
                  });
               }
               break;
            case PLAYER_ABILITIES:
               WrapperPlayServerPlayerAbilities packet = new WrapperPlayServerPlayerAbilities(e);
               data.getAbilityManager().onAbilityServer(packet);
               break;
            case HELD_ITEM_CHANGE:
               WrapperPlayServerHeldItemChange packet = new WrapperPlayServerHeldItemChange(e);
               data.queueToPrePing((uid) -> {
                  data.lastServerSlot = packet.getSlot();
                  data.setCurrentSlot(packet.getSlot());
                  data.setUsingItem(false);
                  data.setEating(false);
               });
               break;
            case ATTACH_ENTITY:
               WrapperPlayServerAttachEntity packet = new WrapperPlayServerAttachEntity(e);
               if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_9)) {
                  return;
               }

               if (!packet.isLeash()) {
                  if (packet.getAttachedId() == data.getEntityId()) {
                     vehicleId = packet.getHoldingId();
                     Entity entity = SpigotReflectionUtil.getEntityById(vehicleId);
                     riding = vehicleId != -1;
                     data.queueToPrePing((uid) -> {
                        data.setRiding(riding);
                        data.setLastUnmount(!riding ? data.getTotalTicks() : data.getLastUnmount());
                        data.setVehicleId(vehicleId);
                        data.setVehicle(entity);
                     });
                     data.setRidingUncertain(vehicleId != -1);
                  } else {
                     vehicleId = packet.getHoldingId();
                     boolean riding = vehicleId != -1;
                     data.queueToPrePing((uid) -> {
                        EntityData edata = (EntityData)data.getEntityData().get(packet.getAttachedId());
                        if (edata != null) {
                           edata.setVehicleId(vehicleId);
                           edata.setRiding(riding);
                        }

                     });
                  }
               }
               break;
            case SET_PASSENGERS:
               WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(e);
               data.queueToPrePing((uid) -> {
                  EntityData eData = (EntityData)data.getEntityData().get(data.getEntityId());
                  if (packet.getPassengers().length == 0) {
                     data.setRiding(false);
                     data.setLastUnmount(data.getTotalTicks());
                     data.setVehicleId(-1);
                     data.setVehicle((Entity)null);
                     if (eData != null) {
                        eData.setRiding(false);
                        eData.setVehicleId(-1);
                     }
                  }

                  int[] var4 = packet.getPassengers();
                  int var5 = var4.length;

                  for(int var6 = 0; var6 < var5; ++var6) {
                     int passengerId = var4[var6];
                     EntityData passengerData = (EntityData)data.getEntityData().get(passengerId);
                     if (passengerId == data.getEntityId()) {
                        data.setRiding(true);
                        data.setVehicleId(packet.getEntityId());
                        data.setVehicle(SpigotReflectionUtil.getEntityById(packet.getEntityId()));
                     }

                     if (passengerData != null) {
                        passengerData.setRiding(true);
                        passengerData.setVehicleId(packet.getEntityId());
                     }
                  }

               });
               break;
            case RESPAWN:
               data.queueToPrePing((uid) -> {
                  data.getCheckManager().runChecks(data.getCheckManager().getPacketChecks(), new RespawnEvent(), (Object)null);
               });
               break;
            case KEEP_ALIVE:
               data.setLastPingTime(nanoTime);
         }

      }
   }

   public void handleTransaction(short number, long nanoTime, KarhuPlayer data) {
      if (data.getScheduledTransactions().containsKey(number)) {
         ++data.sentConfirms;
      }

      if (number >= -20000 && number <= -3000) {
         if (!data.hasSentTickFirst) {
            data.hasSentTickFirst = true;
            data.transactionTime.put(number, nanoTime);
            data.useOldTransaction((uid) -> {
               data.setServerTick(data.getServerTick() + 1L);
            }, number);
         } else {
            data.hasSentTickFirst = false;
         }

         data.sendingPledgePackets = true;
      } else if (!data.sendingPledgePackets && data.getTotalTicks() > 300) {
         Tasker.run(() -> {
            data.getBukkitPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', Karhu.getInstance().getConfigManager().getUninjectedKick()) + " (Time out)");
         });
      }

      int absT = Math.abs(data.getCurrentServerTransaction());
      int absTL = Math.abs(number);
      if (absT - absTL > 1 && absT - absTL < 50 && Karhu.getInstance().getConfigManager().isNethandler() && (Karhu.getInstance().getConfigManager().isDelay() || Karhu.getInstance().getConfigManager().isSpoof())) {
         Karhu.getInstance().printCool("&b> &fTransactions have been skipped, proxy issue? " + absT + " | " + absTL);
      }

      if (number < 0) {
         data.setCurrentServerTransaction(number);
      }

      data.getNetHandler().handleServerTransaction(number, nanoTime);
      data.setFirstTransactionSent(true);
   }

   public static com.github.retrooper.packetevents.protocol.entity.data.EntityData getIndex(List objects, int index) {
      Iterator var2 = objects.iterator();

      com.github.retrooper.packetevents.protocol.entity.data.EntityData object;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         object = (com.github.retrooper.packetevents.protocol.entity.data.EntityData)var2.next();
      } while(object.getIndex() != index);

      return object;
   }
}
