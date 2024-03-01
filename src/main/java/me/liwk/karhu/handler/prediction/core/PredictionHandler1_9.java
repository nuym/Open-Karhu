package me.liwk.karhu.handler.prediction.core;


import com.github.retrooper.packetevents.manager.server.ServerVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.collision.BlockCollisionHandler;
import me.liwk.karhu.handler.collision.enums.CollisionType;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.handler.interfaces.AbstractPredictionHandler;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.VersionBridgeHelper;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.vec.Vec3d;
import me.liwk.karhu.util.player.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class PredictionHandler1_9
    extends AbstractPredictionHandler {
    private boolean lock;
    private boolean onGround;
    private int jumpTicks;
    private boolean allowedJump;
    private float stepHeight = 0.6f;

    public PredictionHandler1_9(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void moveEntityWithHeading() {
        boolean flagSlow;
        boolean flag;
        this.computeKeys();
        this.boundingBox = this.data.getBoundingBox().toBB();
        this.motX = this.data.deltas.lastDX;
        this.motZ = this.data.deltas.lastDZ;
        this.motY = this.data.deltas.lastMotionY;
        if (this.jumpTicks > 0) {
            --this.jumpTicks;
        }
        if (Math.abs(this.motX) < 0.003) {
            this.motX = 0.0;
        }
        if (Math.abs(this.motY) < 0.003) {
            this.motY = 0.0;
        }
        if (Math.abs(this.motZ) < 0.003) {
            this.motZ = 0.0;
        }
        ItemStack s = VersionBridgeHelper.getStackInHand(this.data);
        boolean bl = flag = this.data.lastAttackTick <= 1 && this.data.isSprinting() || s != null && s.hasItemMeta() && s.getItemMeta().hasEnchant(Enchantment.KNOCKBACK);
        if (this.data.isJumpedCurrentTick()) {
            if (this.data.isLastOnGroundPacket() && this.jumpTicks == 0) {
                this.jump();
                this.jumpTicks = 10;
                this.allowedJump = true;
            } else {
                this.allowedJump = false;
            }
        } else {
            this.jumpTicks = 0;
        }
        if (flag) {
            this.motX *= 0.6;
            this.motZ *= 0.6;
        }
        boolean bl2 = flagSlow = this.motY <= 0.0;
        if (flagSlow && this.data.getSlowFallingLevel() > 0) {
            this.motY = 0.01;
        }
        if (!this.data.isOnWater()) {
            if (!this.data.isOnLava()) {
                if (this.data.isGliding()) {
                    Vec3d vec3d = MathUtil.getLook3d(1.0f, this.data);
                    float f = this.data.getLocation().pitch * ((float)Math.PI / 180);
                    double d6 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                    double d8 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
                    double d1 = vec3d.lengthVector();
                    float f4 = MathHelper.cos(f);
                    f4 = (float)((double)f4 * (double)f4 * Math.min(1.0, d1 / 0.4));
                    this.motY += -0.08 + (double)f4 * 0.06;
                    if (this.motY < 0.0 && d6 > 0.0) {
                        double d2 = this.motY * -0.1 * (double)f4;
                        this.motY += d2;
                        this.motX += vec3d.x * d2 / d6;
                        this.motZ += vec3d.z * d2 / d6;
                    }
                    if (f < 0.0f) {
                        double d10 = d8 * (double)(-MathHelper.sin(f)) * 0.04;
                        this.motY += d10 * 3.2;
                        this.motX -= vec3d.x * d10 / d6;
                        this.motZ -= vec3d.z * d10 / d6;
                    }
                    if (d6 > 0.0) {
                        this.motX += (vec3d.x / d6 * d8 - this.motX) * 0.1;
                        this.motZ += (vec3d.z / d6 * d8 - this.motZ) * 0.1;
                    }
                    this.motX *= (double)0.99f;
                    this.motY *= (double)0.98f;
                    this.motZ *= (double)0.99f;
                    this.moveEntity(this.motX, this.motY, this.motZ);
                } else {
                    float f6 = this.data.getCurrentFriction();
                    float f7 = 0.16277136f / (f6 * f6 * f6);
                    double f8 = this.data.isLastOnGroundPacket() ? (double)(this.data.getLastAttributeSpeed() * f7) : (this.data.isWasSprinting() ? 0.025999998673796654 : (double)0.02f);
                    this.moveFlying(this.moveStrafe, this.moveForward, (float)f8);
                    f6 = this.data.getCurrentFriction();
                    if (this.data.isOnClimbable()) {
                        boolean flag1;
                        float f9 = 0.15f;
                        this.motX = MathHelper.clamp_double(this.motX, -f9, f9);
                        this.motZ = MathHelper.clamp_double(this.motZ, -f9, f9);
                        if (this.motY < -0.15) {
                            this.motY = -0.15;
                        }
                        if ((flag1 = this.data.isSneaking()) && this.motY < 0.0) {
                            this.motY = 0.0;
                        }
                    }
                    this.moveEntity(this.motX, this.motY, this.motZ);
                    if (this.data.getLevitationLevel() > 0) {
                        this.motY += (0.05 * (double)this.data.getLevitationLevel() - this.motY) * 0.2;
                    } else if (!this.data.isInUnloadedChunk()) {
                        if (this.hasNoGravity()) {
                            this.motY -= 0.08;
                        }
                    } else {
                        this.motY = this.data.getLocation().y > 0.0 ? -0.1 : 0.0;
                    }
                    this.motY *= (double)0.98f;
                    this.motX *= (double)f6;
                    this.motZ *= (double)f6;
                }
            } else {
                double d4 = this.data.getLocation().y;
                this.moveFlying(this.moveStrafe, this.moveForward, 0.02f);
                this.moveEntity(this.motX, this.motY, this.motZ);
                this.motX *= 0.5;
                this.motY *= 0.5;
                this.motZ *= 0.5;
                if (this.hasNoGravity()) {
                    this.motY -= 0.02;
                }
            }
        } else {
            boolean checkDepthStrider;
            float f1 = this.getWaterSlowDown();
            float f2 = 0.02f;
            float f3 = 0.0f;
            boolean bl3 = checkDepthStrider = Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_7_10) && this.data.getBukkitPlayer().getInventory().getBoots() != null;
            if (checkDepthStrider) {
                f3 = this.data.getBukkitPlayer().getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
            }
            if (f3 > 3.0f) {
                f3 = 3.0f;
            }
            if (!this.data.isLastOnGroundPacket()) {
                f3 *= 0.5f;
            }
            if (f3 > 0.0f) {
                f1 += (0.54600006f - f1) * f3 / 3.0f;
                f2 += (this.data.getLastAttributeSpeed() - f2) * f3 / 3.0f;
            }
            this.moveFlying(this.moveStrafe, this.moveForward, f2);
            this.moveEntity(this.motX, this.motY, this.motZ);
            this.motX *= (double)f1;
            this.motY *= (double)0.8f;
            this.motZ *= (double)f1;
            if (this.hasNoGravity()) {
                this.motY -= 0.02;
            }
        }
        this.nextMotX = this.motX;
        this.nextMotY = this.motY;
        this.nextMotZ = this.motZ;
        this.onGround = this.data.isOnGroundPacket();
    }

    private boolean hasNoGravity() {
        return VersionBridgeHelper.hasGravity(this.data.getBukkitPlayer());
    }

    private float getWaterSlowDown() {
        return 0.8f;
    }

    public void onFireworkUse() {
    }

    @Override
    public void moveFlying(float strafe, float forward, float friction) {
        float f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4f) {
            if ((f = MathHelper.sqrt(f)) < 1.0f) {
                f = 1.0f;
            }
            f = friction / f;
            float f1 = MathHelper.sin(this.data.getLocation().yaw * ((float)Math.PI / 180));
            float f2 = MathHelper.cos(this.data.getLocation().yaw * ((float)Math.PI / 180));
            this.motX += (double)((strafe *= f) * f2 - (forward *= f) * f1);
            this.motY += 0.0;
            this.motZ += (double)(forward * f2 + strafe * f1);
        }
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        if (this.data.isInWeb()) {
            x *= 0.25;
            y *= (double)0.05f;
            z *= 0.25;
            this.motX = 0.0;
            this.motY = 0.0;
            this.motZ = 0.0;
        }
        double d2 = x;
        double d3 = y;
        double d4 = z;
        if (this.data.isLastOnGroundPacket() && this.data.isSneaking()) {
            double d5 = 0.05;
            while (x != 0.0 && this.karhu.getNmsWorldProvider().getCollidingBoundingBoxes(this.data.getBukkitPlayer(), this.getEntityBoundingBox().offset(x, -this.stepHeight, 0.0)).isEmpty()) {
                x = x < 0.05 && x >= -0.05 ? 0.0 : (x > 0.0 ? (x -= 0.05) : (x += 0.05));
                d2 = x;
            }
            while (z != 0.0 && this.karhu.getNmsWorldProvider().getCollidingBoundingBoxes(this.data.getBukkitPlayer(), this.getEntityBoundingBox().offset(0.0, -this.stepHeight, z)).isEmpty()) {
                z = z < 0.05 && z >= -0.05 ? 0.0 : (z > 0.0 ? (z -= 0.05) : (z += 0.05));
                d4 = z;
            }
            while (x != 0.0 && z != 0.0 && this.karhu.getNmsWorldProvider().getCollidingBoundingBoxes(this.data.getBukkitPlayer(), this.getEntityBoundingBox().offset(x, -this.stepHeight, z)).isEmpty()) {
                x = x < 0.05 && x >= -0.05 ? 0.0 : (x > 0.0 ? (x -= 0.05) : (x += 0.05));
                d2 = x;
                z = z < 0.05 && z >= -0.05 ? 0.0 : (z > 0.0 ? (z -= 0.05) : (z += 0.05));
                d4 = z;
            }
        }
        boolean flag = this.data.isLastOnGroundPacket() || d3 != y && d3 < 0.0;
        int j6 = MathHelper.floor_double(this.data.getLocation().x);
        int i1 = MathHelper.floor_double(this.data.getLocation().y - (double)0.2f);
        int k6 = MathHelper.floor_double(this.data.getLocation().z);
        Block block1 = null;
        Location location = new Location(this.data.getWorld(), j6, i1, k6);
        if (BlockUtil.chunkLoaded(location)) {
            Block block;
            block1 = location.getBlock();
            if (location.getBlock().getType() == Material.AIR && MaterialChecks.FENCES.contains((block = (location = location.subtract(0.0, 1.0, 0.0)).getBlock()).getType())) {
                block1 = block;
            }
        }
        if (d2 != x) {
            this.motX = 0.0;
        }
        if (d4 != z) {
            this.motZ = 0.0;
        }
        if (d3 != y && block1 != null) {
            BlockCollisionHandler.run(block1, CollisionType.LANDED, this);
        }
        if (!this.data.getBukkitPlayer().isFlying() && !flag && this.data.getBukkitPlayer().getVehicle() == null) {
            if (this.data.isLastOnGroundPacket() && block1 != null) {
                BlockCollisionHandler.run(block1, CollisionType.WALKING, this);
            }
            this.doBlockCollisions();
        }
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isLock() {
        return this.lock;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public int getJumpTicks() {
        return this.jumpTicks;
    }

    public boolean isAllowedJump() {
        return this.allowedJump;
    }
}
