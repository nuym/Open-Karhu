package me.liwk.karhu.util.mc.facing;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.facing.EnumFacing.EnumFacing;
import me.liwk.karhu.util.mc.string.IStringSerializable;
import me.liwk.karhu.util.mc.vec.Vec3i;

public enum EnumFacing implements IStringSerializable {
   DOWN("DOWN", 0, 0, 1, -1, "down", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Y, new Vec3i(0, -1, 0)),
   UP("UP", 1, 1, 0, -1, "up", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Y, new Vec3i(0, 1, 0)),
   NORTH("NORTH", 2, 2, 3, 2, "north", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, -1)),
   SOUTH("SOUTH", 3, 3, 2, 0, "south", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, 1)),
   WEST("WEST", 4, 4, 5, 1, "west", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X, new Vec3i(-1, 0, 0)),
   EAST("EAST", 5, 5, 4, 3, "east", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X, new Vec3i(1, 0, 0));

   private final int index;
   private final int opposite;
   private final int horizontalIndex;
   private final String name;
   private final Axis axis;
   private final AxisDirection axisDirection;
   private final Vec3i directionVec;
   public static final EnumFacing[] VALUES = new EnumFacing[6];
   private static final EnumFacing[] HORIZONTALS = new EnumFacing[4];
   private static final Map NAME_LOOKUP = Maps.newHashMap();
   private static final EnumFacing[] $VALUES = new EnumFacing[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
   private static final String __OBFID = "CL_00001201";

   private EnumFacing(String p_i17_3_, int p_i17_4_, int p_i17_5_, int p_i17_6_, int p_i17_7_, String p_i17_8_, AxisDirection p_i17_9_, Axis p_i17_10_, Vec3i p_i17_11_) {
      this.index = p_i17_5_;
      this.horizontalIndex = p_i17_7_;
      this.opposite = p_i17_6_;
      this.name = p_i17_8_;
      this.axis = p_i17_10_;
      this.axisDirection = p_i17_9_;
      this.directionVec = p_i17_11_;
   }

   public int getIndex() {
      return this.index;
   }

   public int getHorizontalIndex() {
      return this.horizontalIndex;
   }

   public AxisDirection getAxisDirection() {
      return this.axisDirection;
   }

   public EnumFacing getOpposite() {
      return VALUES[this.opposite];
   }

   public EnumFacing rotateAround(Axis axis) {
      switch (1.field_179515_a[axis.ordinal()]) {
         case 1:
            if (this != WEST && this != EAST) {
               return this.rotateX();
            }

            return this;
         case 2:
            if (this != UP && this != DOWN) {
               return this.rotateY();
            }

            return this;
         case 3:
            if (this != NORTH && this != SOUTH) {
               return this.rotateZ();
            }

            return this;
         default:
            throw new IllegalStateException("Unable to get CW facing for axis " + axis);
      }
   }

   public EnumFacing rotateY() {
      switch (1.field_179513_b[this.ordinal()]) {
         case 1:
            return EAST;
         case 2:
            return SOUTH;
         case 3:
            return WEST;
         case 4:
            return NORTH;
         default:
            throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
      }
   }

   private EnumFacing rotateX() {
      switch (1.field_179513_b[this.ordinal()]) {
         case 1:
            return DOWN;
         case 2:
         case 4:
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + this);
         case 3:
            return UP;
         case 5:
            return NORTH;
         case 6:
            return SOUTH;
      }
   }

   private EnumFacing rotateZ() {
      switch (1.field_179513_b[this.ordinal()]) {
         case 2:
            return DOWN;
         case 3:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case 4:
            return UP;
         case 5:
            return EAST;
         case 6:
            return WEST;
      }
   }

   public EnumFacing rotateYCCW() {
      switch (1.field_179513_b[this.ordinal()]) {
         case 1:
            return WEST;
         case 2:
            return NORTH;
         case 3:
            return EAST;
         case 4:
            return SOUTH;
         default:
            throw new IllegalStateException("Unable to get CCW facing of " + this);
      }
   }

   public int getFrontOffsetX() {
      return this.axis == EnumFacing.Axis.X ? this.axisDirection.getOffset() : 0;
   }

   public int getFrontOffsetY() {
      return this.axis == EnumFacing.Axis.Y ? this.axisDirection.getOffset() : 0;
   }

   public int getFrontOffsetZ() {
      return this.axis == EnumFacing.Axis.Z ? this.axisDirection.getOffset() : 0;
   }

   public String getName2() {
      return this.name;
   }

   public Axis getAxis() {
      return this.axis;
   }

   public static EnumFacing byName(String name) {
      return name == null ? null : (EnumFacing)NAME_LOOKUP.get(name.toLowerCase());
   }

   public static EnumFacing getFront(int index) {
      return VALUES[MathHelper.abs_int(index % VALUES.length)];
   }

   public static EnumFacing getHorizontal(int p_176731_0_) {
      return HORIZONTALS[MathHelper.abs_int(p_176731_0_ % HORIZONTALS.length)];
   }

   public static EnumFacing fromAngle(double angle) {
      return getHorizontal(MathHelper.floor_double(angle / 90.0 + 0.5) & 3);
   }

   public static EnumFacing random(Random rand) {
      return values()[rand.nextInt(values().length)];
   }

   public static EnumFacing getFacingFromVector(float p_176737_0_, float p_176737_1_, float p_176737_2_) {
      EnumFacing enumfacing = NORTH;
      float f = Float.MIN_VALUE;
      EnumFacing[] var5 = values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         EnumFacing enumfacing1 = var5[var7];
         float f1 = p_176737_0_ * (float)enumfacing1.directionVec.getX() + p_176737_1_ * (float)enumfacing1.directionVec.getY() + p_176737_2_ * (float)enumfacing1.directionVec.getZ();
         if (f1 > f) {
            f = f1;
            enumfacing = enumfacing1;
         }
      }

      return enumfacing;
   }

   public String toString() {
      return this.name;
   }

   public String getName() {
      return this.name;
   }

   public static EnumFacing func_181076_a(AxisDirection p_181076_0_, Axis p_181076_1_) {
      EnumFacing[] var2 = values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EnumFacing enumfacing = var2[var4];
         if (enumfacing.getAxisDirection() == p_181076_0_ && enumfacing.getAxis() == p_181076_1_) {
            return enumfacing;
         }
      }

      throw new IllegalArgumentException("No such direction: " + p_181076_0_ + " " + p_181076_1_);
   }

   public Vec3i getDirectionVec() {
      return this.directionVec;
   }

   static {
      EnumFacing[] var0 = values();
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         EnumFacing enumfacing = var0[var2];
         VALUES[enumfacing.index] = enumfacing;
         if (enumfacing.getAxis().isHorizontal()) {
            HORIZONTALS[enumfacing.horizontalIndex] = enumfacing;
         }

         NAME_LOOKUP.put(enumfacing.getName2().toLowerCase(), enumfacing);
      }

   }

   public static enum Plane implements Predicate, Iterable {
      HORIZONTAL("HORIZONTAL", 0),
      VERTICAL("VERTICAL", 1);

      private static final Plane[] $VALUES = new Plane[]{HORIZONTAL, VERTICAL};
      private static final String __OBFID = "CL_00002319";

      private Plane(String p_i16_3_, int p_i16_4_) {
      }

      public EnumFacing[] facings() {
         switch (1.field_179514_c[this.ordinal()]) {
            case 1:
               return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
            case 2:
               return new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
            default:
               throw new Error("Someone's been tampering with the universe!");
         }
      }

      public EnumFacing random(Random rand) {
         EnumFacing[] aenumfacing = this.facings();
         return aenumfacing[rand.nextInt(aenumfacing.length)];
      }

      public boolean apply(EnumFacing p_apply_1_) {
         return p_apply_1_ != null && p_apply_1_.getAxis().getPlane() == this;
      }

      public Iterator iterator() {
         return Iterators.forArray(this.facings());
      }

      public boolean apply(Object p_apply_1_) {
         return this.apply((EnumFacing)p_apply_1_);
      }
   }

   public static enum AxisDirection {
      POSITIVE("POSITIVE", 0, 1, "Towards positive"),
      NEGATIVE("NEGATIVE", 1, -1, "Towards negative");

      private final int offset;
      private final String description;
      private static final AxisDirection[] $VALUES = new AxisDirection[]{POSITIVE, NEGATIVE};
      private static final String __OBFID = "CL_00002320";

      private AxisDirection(String p_i15_3_, int p_i15_4_, int p_i15_5_, String p_i15_6_) {
         this.offset = p_i15_5_;
         this.description = p_i15_6_;
      }

      public int getOffset() {
         return this.offset;
      }

      public String toString() {
         return this.description;
      }
   }

   public static enum Axis implements Predicate, IStringSerializable {
      X("X", 0, "x", EnumFacing.Plane.HORIZONTAL),
      Y("Y", 1, "y", EnumFacing.Plane.VERTICAL),
      Z("Z", 2, "z", EnumFacing.Plane.HORIZONTAL);

      private static final Map NAME_LOOKUP = Maps.newHashMap();
      private final String name;
      private final Plane plane;
      private static final Axis[] $VALUES = new Axis[]{X, Y, Z};
      private static final String __OBFID = "CL_00002321";

      private Axis(String p_i14_3_, int p_i14_4_, String p_i14_5_, Plane p_i14_6_) {
         this.name = p_i14_5_;
         this.plane = p_i14_6_;
      }

      public static Axis byName(String name) {
         return name == null ? null : (Axis)NAME_LOOKUP.get(name.toLowerCase());
      }

      public String getName2() {
         return this.name;
      }

      public boolean isVertical() {
         return this.plane == EnumFacing.Plane.VERTICAL;
      }

      public boolean isHorizontal() {
         return this.plane == EnumFacing.Plane.HORIZONTAL;
      }

      public String toString() {
         return this.name;
      }

      public boolean apply(EnumFacing p_apply_1_) {
         return p_apply_1_ != null && p_apply_1_.getAxis() == this;
      }

      public Plane getPlane() {
         return this.plane;
      }

      public String getName() {
         return this.name;
      }

      public boolean apply(Object p_apply_1_) {
         return this.apply((EnumFacing)p_apply_1_);
      }

      static {
         Axis[] var0 = values();
         int var1 = var0.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            Axis enumfacing$axis = var0[var2];
            NAME_LOOKUP.put(enumfacing$axis.getName2().toLowerCase(), enumfacing$axis);
         }

      }
   }
}
