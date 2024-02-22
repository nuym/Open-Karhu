package me.liwk.karhu.handler.collision.type;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;

public class MaterialChecks {
   public static Set AIR = null;
   public static Set MOVABLE = null;
   public static Set SHULKER_BOXES = null;
   public static Set ICE = null;
   public static Set SIGNS = null;
   public static Set HALFS = null;
   public static Set GRASS = null;
   public static Set DOORS = null;
   public static Set TRAPS = null;
   public static Set LIQUIDS = null;
   public static Set WATER = null;
   public static Set LAVA = null;
   public static Set SEASHIT = null;
   public static Set FENCES = null;
   public static Set PANES = null;
   public static Set WEIRD_SOLID = null;
   public static Set WEIRD_SOLID_NO_LIQUID = null;
   public static Set STAIRS = null;
   public static Set BED = null;
   public static Set LILY = null;
   public static Set WEB = null;
   public static Set SLIME = null;
   public static Set SOUL = null;
   public static Set HONEY = null;
   public static Set BERRIES = null;
   public static Set SCAFFOLD = null;
   public static Set CLIMBABLE = null;
   public static Set REDSTONE = null;
   public static Set CARPETS = null;
   public static Set ONETAPS = null;
   public static Set BUTTONS = null;
   public static Set TORCHES = null;
   public static Set RETARD_FACE = null;
   public static Set PORTAL = null;
   public static Set POWDERSNOW = null;
   public static Set DRIP_LEAF = null;
   public static Set EDIBLE_WITHOUT_HUNGER = null;
   public static Set SWORDS = null;
   public static Set BOWS = null;
   public static Set LIQUID_BUCKETS = null;
   public static Set CLEARICE = null;
   public static Set PACKEDICE = null;
   public static Set FROSTEDICE = null;
   public static Set BLUEICE = null;

   public static Set find(String... array) {
      Set mats = new HashSet();
      String[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String shits = var2[var4];
         Material[] var6 = Material.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Material c = var6[var8];
            if (c.name().contains(shits)) {
               mats.add(c);
            }
         }
      }

      return mats;
   }

   public static Set fastFind(String... array) {
      Set mats = new HashSet();
      String[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String shits = var2[var4];

         try {
            Material material = Material.valueOf(shits);
            mats.add(material);
         } catch (IllegalArgumentException var7) {
         }
      }

      return mats;
   }

   static {
      try {
         AIR = fastFind("AIR", "CAVE_AIR", "VOID_AIR");
         BED = find("BED");
         MOVABLE = find("SHULKER_BOX", "PISTON");
         SHULKER_BOXES = find("SHULKER_BOX");
         FENCES = find("FENCE", "GATE", "WALL", "COBBLE_WALL", "PANE", "THIN");
         PANES = find("PANE", "THIN");
         ICE = find("ICE", "PACKED");
         GRASS = find("GRASS", "FLOWER", "ROSE");
         PORTAL = find("PORTAL_FRAME");
         DOORS = find("DOOR");
         TRAPS = find("TRAP");
         HALFS = find("SLAB", "STEP", "DAYLIGHT", "SENSOR", "SNOW", "SKULL", "HEAD", "CAKE", "POT", "BEAN", "COCOA", "ENCH", "STONECUTTER", "LANTERN", "CAMPFIRE", "CANDLE", "PICKLE", "BELL", "AMETHYST", "BED");
         STAIRS = find("STAIR");
         SIGNS = find("SIGN");
         CARPETS = find("CARPET");
         REDSTONE = find("DIODE", "REPEATER", "COMPARATOR");
         WEIRD_SOLID = find("LILY", "COCOA", "REDSTONE_", "POT", "ROD", "CARPET", "WATER", "BUBBLE", "LAVA", "SKULL", "LADDER", "SNOW", "SCAFFOLD", "DIODE", "REPEATER", "COMPARATOR", "VINE", "CANDLE", "PICKLE", "DRIP_LEAF");
         WEIRD_SOLID_NO_LIQUID = find("LILY", "COCOA", "REDSTONE_", "POT", "ROD", "CARPET", "SKULL", "LADDER", "SNOW", "SCAFFOLD", "DIODE", "REPEATER", "COMPARATOR", "VINE", "CANDLE", "PICKLE", "DRIP_LEAF");
         LIQUIDS = fastFind("WATER", "STATIONARY_WATER", "LAVA", "STATIONARY_LAVA");
         WATER = find("WATER", "STATIONARY_WATER", "BUBBLE_COLUMN");
         LAVA = find("LAVA", "STATIONARY_LAVA");
         SEASHIT = find("KELP", "SEAGRASS");
         LILY = find("LILY");
         DRIP_LEAF = find("DRIPLEAF");
         WEB = find("WEB");
         SLIME = find("SLIME_BLOCK");
         SOUL = fastFind("SOUL_SAND");
         HONEY = find("HONEY_BLOCK");
         BERRIES = find("SWEET");
         SCAFFOLD = find("SCAFFOLDING");
         POWDERSNOW = find("POWDER_SNOW");
         CLIMBABLE = find("VINE", "LADDER", "SCAFFOLDING");
         ONETAPS = find("SLIME_BLOCK", "FLOWER", "ROSE", "TORCH");
         BUTTONS = find("BUTTON");
         TORCHES = find("TORCH");
         RETARD_FACE = find("TORCH", "BUTTON", "SIGN");
         EDIBLE_WITHOUT_HUNGER = find("GOLDEN_APPLE", "POTION", "BOTTLE", "MILK_BUCKET");
         SWORDS = find("SWORD");
         BOWS = find("BOW");
         LIQUID_BUCKETS = fastFind("WATER_BUCKET", "LAVA_BUCKET");
         CLEARICE = fastFind("ICE");
         PACKEDICE = fastFind("PACKED_ICE");
         FROSTEDICE = fastFind("FROSTED_ICE");
         BLUEICE = fastFind("BLUE_ICE");
      } catch (Exception var1) {
         Exception ex = var1;
         ex.printStackTrace();
      }

   }
}
