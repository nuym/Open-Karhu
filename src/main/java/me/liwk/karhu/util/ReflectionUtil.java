package me.liwk.karhu.util;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReflectionUtil {
   private static String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
   public static Class blockPosition = null;
   private static Class iBlockData = null;
   private static Class craftWorld = getCBClass("CraftWorld");
   private static Class worldServer = getNMSClass("WorldServer");

   public static boolean canDestroyBlock(KarhuPlayer data, Block block) {
      Object inventory = getVanillaInventory(data.getBukkitPlayer());
      return (Boolean)getMethodValue(getMethod(getNMSClass("PlayerInventory"), "b", getNMSClass("Block")), inventory, Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2) ? getBlockData(block) : getVanillaBlock(block));
   }

   public static Object getVanillaInventory(Player player) {
      return getMethodValue(getMethod(getCBClass("inventory.CraftInventoryPlayer"), "getInventory"), player.getInventory());
   }

   public static float getDestroySpeed(Block block, KarhuPlayer data) {
      Object item = getVanillaItem(data.getStackInHand());
      return Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_8_8) ? (Float)getMethodValue(getMethod(getNMSClass("Item"), "getDestroySpeed", getNMSClass("ItemStack"), getNMSClass("IBlockData")), item, getVanillaItemStack(data.getStackInHand()), getBlockData(block)) : (Float)getMethodValue(getMethod(getNMSClass("Item"), "getDestroySpeed", getNMSClass("ItemStack"), getNMSClass("Block")), item, getVanillaItemStack(data.getStackInHand()), getVanillaBlock(block));
   }

   public static float getBlockDurability(Block block) {
      Object vanillaBlock = getVanillaBlock(block);
      if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_16)) {
         Object getType = getBlockData(block);
         Object blockData = getMethodValue(getMethod(getNMSClass("Block"), "getBlockData"), vanillaBlock);
         return (Float)getFieldValue(getFieldByName(iBlockData, "strength"), blockData);
      } else {
         return (Float)getFieldValue(getFieldByName(getNMSClass("Block"), "strength"), vanillaBlock);
      }
   }

   public static Object getVanillaBlock(Block block) {
      Object getType;
      if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         getType = getWorldHandle(block.getWorld());
         return getMethodValue(getMethod(worldServer, "getType", Integer.TYPE, Integer.TYPE, Integer.TYPE), getType, block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
      } else {
         getType = getBlockData(block);
         return getMethodValue(getMethod(iBlockData, "getBlock"), getType);
      }
   }

   private static Object getBlockData(Block block) {
      try {
         Object bPos;
         if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            bPos = getWorldHandle(block.getWorld());
            return getMethodValue(getMethod(worldServer, "getType", Integer.TYPE, Integer.TYPE, Integer.TYPE), bPos, block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
         } else {
            bPos = blockPosition.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
            Object world = getWorldHandle(block.getWorld());
            return getMethodValue(getMethod(worldServer, "getType", blockPosition), world, bPos);
         }
      } catch (Exception var3) {
         Exception e = var3;
         e.printStackTrace();
         return null;
      }
   }

   public static Object getVanillaItem(ItemStack itemStack) {
      return getMethodValue(getMethod(getNMSClass("ItemStack"), "getItem"), getVanillaItemStack(itemStack));
   }

   public static Object getVanillaItemStack(ItemStack itemStack) {
      return getMethodValue(getMethod(getCBClass("inventory.CraftItemStack"), "asNMSCopy", getClass("org.bukkit.inventory.ItemStack")), itemStack, itemStack);
   }

   public static Object getMethodValue(Method method, Object object, Object... args) {
      try {
         return method.invoke(object, args);
      } catch (Exception var4) {
         Exception e = var4;
         e.printStackTrace();
         return null;
      }
   }

   public static Method getMethod(Class clazz, String methodName, Class... args) {
      try {
         Method method = clazz.getMethod(methodName, args);
         method.setAccessible(true);
         return method;
      } catch (Exception var4) {
         Exception e = var4;
         e.printStackTrace();
         return null;
      }
   }

   public static Class getNMSClass(String string) {
      return getClass("net.minecraft.server." + version + "." + string);
   }

   public static Class getCBClass(String string) {
      return getClass("org.bukkit.craftbukkit." + version + "." + string);
   }

   public static Class getClass(String string) {
      try {
         return Class.forName(string);
      } catch (ClassNotFoundException var2) {
         ClassNotFoundException e = var2;
         e.printStackTrace();
         return null;
      }
   }

   public static Object getFieldValue(Field field, Object object) {
      try {
         field.setAccessible(true);
         return field.get(object);
      } catch (Exception var3) {
         Exception e = var3;
         e.printStackTrace();
         return null;
      }
   }

   public static Field getFieldByName(Class clazz, String fieldName) {
      try {
         Field field = clazz.getDeclaredField(fieldName) != null ? clazz.getDeclaredField(fieldName) : clazz.getSuperclass().getDeclaredField(fieldName);
         field.setAccessible(true);
         return field;
      } catch (Exception var3) {
         Exception e = var3;
         e.printStackTrace();
         return null;
      }
   }

   public static Object getWorldHandle(World world) {
      return getMethodValue(getMethod(craftWorld, "getHandle"), world);
   }

   static {
      if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_8)) {
         iBlockData = getNMSClass("IBlockData");
         blockPosition = getNMSClass("BlockPosition");
      }

   }
}
