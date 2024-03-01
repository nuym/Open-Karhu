package me.liwk.karhu.handler.prediction.core;


import com.github.retrooper.packetevents.manager.server.ServerVersion;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.interfaces.AbstractPredictionHandler;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import org.bukkit.enchantments.Enchantment;

public final class PredictionHandler
    extends AbstractPredictionHandler {
    private int jumpTicks;
    private boolean lock;
    private boolean allowedJump;
    private float stepHeight = 0.6f;

    public PredictionHandler(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void moveEntityWithHeading() {
        this.boundingBox = this.data.getBoundingBox().toBB();
        this.computeKeys();
        this.motX = this.data.deltas.lastDX;
        this.motZ = this.data.deltas.lastDZ;
        this.motY = this.data.deltas.lastMotionY;
        if (this.jumpTicks > 0) {
            --this.jumpTicks;
        }
        if (Math.abs(this.motX) < 0.005) {
            this.motX = 0.0;
        }
        if (Math.abs(this.motY) < 0.005) {
            this.motY = 0.0;
        }
        if (Math.abs(this.motZ) < 0.005) {
            this.motZ = 0.0;
        }
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
        if (!this.data.isOnWater()) {
            if (!this.data.isOnLava()) {
                float f4 = this.data.getCurrentFriction();
                float f = 0.16277136f / (f4 * f4 * f4);
                float f5 = this.data.isLastOnGroundPacket() ? this.data.getAttributeSpeed() * f : (this.data.isWasSprinting() ? 0.026f : 0.02f);
                this.moveFlying(this.moveStrafe, this.moveForward, f5);
                if (this.data.isOnClimbable()) {
                    boolean flag;
                    float f6 = 0.15f;
                    this.motX = MathHelper.clamp_double(this.motX, -f6, f6);
                    this.motZ = MathHelper.clamp_double(this.motZ, -f6, f6);
                    if (this.motY < -0.15) {
                        this.motY = -0.15;
                    }
                    if ((flag = this.data.isSneaking()) && this.motY < 0.0) {
                        this.motY = 0.0;
                    }
                }
                this.moveEntity(this.motX, this.motY, this.motZ);
                if (this.data.isCollidedHorizontally() && this.data.isOnClimbable()) {
                    this.motY = 0.2;
                }
                this.motY = this.data.isInUnloadedChunk() ? (this.data.getLocation().getY() > 0.0 ? -0.1 : 0.0) : (this.motY -= 0.08);
                this.motY *= (double)0.98f;
                this.motX *= (double)f4;
                this.motZ *= (double)f4;
            } else {
                this.moveFlying(this.moveStrafe, this.moveForward, 0.02f);
                this.moveEntity(this.motX, this.motY, this.motZ);
                this.motX *= 0.5;
                this.motY *= 0.5;
                this.motZ *= 0.5;
                this.motY -= 0.02;
            }
        } else {
            boolean checkDepthStrider = Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_7_10) && this.data.getBukkitPlayer().getInventory().getBoots() != null;
            float f1 = 0.8f;
            float f2 = 0.02f;
            float f3 = 0.0f;
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
                f2 += (this.data.getLastAttributeSpeed() * 1.0f - f2) * f3 / 3.0f;
            }
            this.moveFlying(this.moveStrafe, this.moveForward, f2);
            this.moveEntity(this.motX, this.motY, this.motZ);
            this.motZ *= (double)f1;
            this.motY *= (double)0.8f;
            this.motZ *= (double)f1;
            this.motY -= 0.02;
        }
        if (this.data.isJumped()) {
            if (this.data.isOnLiquid()) {
                this.motY += (double)0.04f;
            } else if (this.data.isLastOnGroundPacket() && this.jumpTicks == 0) {
                this.jump();
                this.jumpTicks = 10;
            }
        } else {
            this.jumpTicks = 0;
        }
        this.nextMotX = this.motX;
        this.nextMotY = this.motY;
        this.nextMotZ = this.motZ;
        this.predictedLocation = new CustomLocation(this.data.getLocation().getX() + this.nextMotX, this.data.getLocation().getY() + this.nextMotY, this.data.getLocation().getZ() + this.nextMotZ);
    }

    @Override
    public void moveFlying(float strafe, float forward, float friction) {
        float f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4f) {
            if ((f = MathHelper.sqrt_float(f)) < 1.0f) {
                f = 1.0f;
            }
            float magic = this.data.getClientVersion() != null && this.data.isNewerThan8() ? (float)Math.PI / 180 : 0.0f;
            f = friction / f;
            float f1 = MathHelper.sin(this.data.getLocation().getYaw() * magic != 0.0f ? magic : (float)Math.PI / 180);
            float f2 = MathHelper.cos(this.data.getLocation().getYaw() * magic != 0.0f ? magic : (float)Math.PI / 180);
            this.motX += (double)((strafe *= f) * f2 - (forward *= f) * f1);
            this.motZ += (double)(forward * f2 + strafe * f1);
        }
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        boolean flag;
        if (this.data.isInWeb()) {
            x *= 0.25;
            y *= (double)0.05f;
            z *= 0.25;
            this.motX = 0.0;
            this.motY = 0.0;
            this.motZ = 0.0;
        }
        double d3 = x;
        double d4 = y;
        double d5 = z;
        boolean bl = flag = this.data.isLastOnGroundPacket() && this.data.isSneaking();
        if (flag) {
            double d6 = 0.05;
            while (x != 0.0 && this.karhu.getNmsWorldProvider().getCollidingBoundingBoxes(this.data.getBukkitPlayer(), this.getEntityBoundingBox().offset(x, -1.0, 0.0)).isEmpty()) {
                x = x < d6 && x >= -d6 ? 0.0 : (x > 0.0 ? (x -= d6) : (x += d6));
                d3 = x;
            }
            while (z != 0.0 && this.karhu.getNmsWorldProvider().getCollidingBoundingBoxes(this.data.getBukkitPlayer(), this.getEntityBoundingBox().offset(0.0, -1.0, z)).isEmpty()) {
                z = z < d6 && z >= -d6 ? 0.0 : (z > 0.0 ? (z -= d6) : (z += d6));
                d5 = z;
            }
            while (x != 0.0 && z != 0.0 && this.karhu.getNmsWorldProvider().getCollidingBoundingBoxes(this.data.getBukkitPlayer(), this.getEntityBoundingBox().offset(x, -1.0, z)).isEmpty()) {
                x = x < d6 && x >= -d6 ? 0.0 : (x > 0.0 ? (x -= d6) : (x += d6));
                d3 = x;
                z = z < d6 && z >= -d6 ? 0.0 : (z > 0.0 ? (z -= d6) : (z += d6));
                d5 = z;
            }
        }
        int i = MathHelper.floor_double(this.data.getLocation().x);
        int j = MathHelper.floor_double(this.data.getLocation().y - (double)0.2f);
        int k = MathHelper.floor_double(this.data.getLocation().z);
        if (d3 != x) {
            this.motX = 0.0;
        }
        if (d5 != z) {
            this.motZ = 0.0;
        }
        if (d4 != y) {
            // empty if block
        }
        if (this.data.getBukkitPlayer().isFlying() || flag || this.data.getBukkitPlayer().getVehicle() != null || this.data.isLastOnGroundPacket()) {
            // empty if block
        }
    }

    public int getJumpTicks() {
        return this.jumpTicks;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLock() {
        return this.lock;
    }

    public boolean isAllowedJump() {
        return this.allowedJump;
    }
}
