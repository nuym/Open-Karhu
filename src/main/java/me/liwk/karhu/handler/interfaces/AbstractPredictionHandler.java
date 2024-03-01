package me.liwk.karhu.handler.interfaces;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.handler.collision.BlockCollisionHandler;
import me.liwk.karhu.handler.collision.enums.CollisionType;
import me.liwk.karhu.handler.prediction.core.PredictionHandler;
import me.liwk.karhu.handler.prediction.core.PredictionHandler1_9;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.BlockPos;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.pair.Pair;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.world.nms.NMSValueParser;
import org.bukkit.Location;
import org.bukkit.block.Block;

public abstract class AbstractPredictionHandler {
    public double motX;
    public double motZ;
    public double motY;
    public double nextMotX;
    public double nextMotZ;
    public double nextMotY;
    public float moveStrafe;
    public float moveForward;
    public CustomLocation predictedLocation;
    protected final KarhuPlayer data;
    protected final Karhu karhu;
    protected AxisAlignedBB boundingBox;

    public AbstractPredictionHandler(KarhuPlayer data, Karhu karhu) {
        this.data = data;
        this.karhu = karhu;
    }

    protected final void jump() {
        this.motY = 0.42f;
        this.motY += (double)((float)this.data.getJumpBoost() * 0.1f);
        float f = this.data.getLocation().getYaw() * ((float)Math.PI / 180);
        this.motX -= (double)(MathHelper.sin(f) * 0.2f);
        this.motZ += (double)(MathHelper.cos(f) * 0.2f);
    }

    protected final void doBlockCollisions() {
        BlockPos blockpos = new BlockPos(this.getEntityBoundingBox().minX + 0.001, this.getEntityBoundingBox().minY + 0.001, this.getEntityBoundingBox().minZ + 0.001);
        BlockPos blockpos1 = new BlockPos(this.getEntityBoundingBox().maxX - 0.001, this.getEntityBoundingBox().maxY - 0.001, this.getEntityBoundingBox().maxZ - 0.001);
        if (this.karhu.getNmsWorldProvider().isAreaLoaded(this.data.getWorld(), blockpos, blockpos1)) {
            for (int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
                for (int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                    for (int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
                        Location l = new Location(this.data.getWorld(), i, j, k);
                        Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(l);
                        if (block == null) continue;
                        BlockCollisionHandler.run(block, CollisionType.WALKING, this);
                    }
                }
            }
        }
    }

    protected final void computeKeys() {
        float motionAngle = MathUtil.getMoveAngleNoAbs(this.data.getLastLocation(), this.data.getLocation()) / 45.0f;
        int dir = 6;
        String key = "-";
        if (Math.abs(Math.abs(this.data.deltas.deltaX) + Math.abs(this.data.deltas.deltaZ)) > 1.0E-12) {
            dir = (int)new BigDecimal(motionAngle).setScale(1, RoundingMode.HALF_UP).doubleValue();
            switch (dir) {
                case 1: {
                    this.moveForward = 1.0f;
                    this.moveStrafe = -1.0f;
                    key = "W + D";
                    break;
                }
                case 2: {
                    this.moveStrafe = -1.0f;
                    key = "D";
                    break;
                }
                case 3: {
                    this.moveForward = -1.0f;
                    this.moveStrafe = -1.0f;
                    key = "S + D";
                    break;
                }
                case 4: {
                    this.moveForward = -1.0f;
                    key = "S";
                    break;
                }
                case -3: {
                    this.moveForward = -1.0f;
                    this.moveStrafe = 1.0f;
                    key = "S + A";
                    break;
                }
                case -2: {
                    this.moveStrafe = 1.0f;
                    key = "A";
                    break;
                }
                case -1: {
                    this.moveForward = 1.0f;
                    this.moveStrafe = 1.0f;
                    key = "W + A";
                    break;
                }
                case 8: {
                    this.moveForward = 1.0f;
                    key = "W";
                    break;
                }
                case 0: {
                    this.moveForward = 1.0f;
                    key = "W";
                }
            }
        }
        if (this.data.isSneaking()) {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3);
            this.moveForward = (float)((double)this.moveForward * 0.3);
        }
        this.moveForward *= 0.98f;
        this.moveStrafe *= 0.98f;
        if (this.data.isUsingItem()) {
            this.moveForward *= 0.2f;
            this.moveStrafe *= 0.2f;
        }
        this.data.setKeyCombo(key);
    }

    protected final void init() {
        Pair motions;
        double x = this.motX;
        double z = this.motZ;
        this.motZ = 0.0;
        this.motX = 0.0;
        HashMap<Double, Pair> dataAssessments = new HashMap<Double, Pair>();
        for (float[] floats : NMSValueParser.KEY_COMBOS) {
            float forward = floats[1];
            float strafe = floats[0];
            if (this.data.isSneaking()) {
                strafe = (float)((double)strafe * 0.3);
                forward = (float)((double)forward * 0.3);
            }
            forward *= 0.98f;
            strafe *= 0.98f;
            if (this.data.isUsingItem()) {
                forward *= 0.2f;
                strafe *= 0.2f;
            }
            if (this.data.isJumped() && this.data.elapsed(this.data.getLastInLiquid()) > 1) {
                this.jump();
            }
            this.moveFlying(strafe, forward, PlayerUtil.getScaledFriction(this.data));
            dataAssessments.put(MathUtil.hypot(new double[]{this.data.deltas.deltaX - this.motX, this.data.deltas.deltaZ - this.motZ}), new Pair(Float.valueOf(strafe), Float.valueOf(forward)));
            this.motX = 0.0;
            this.motZ = 0.0;
        }
        this.motX = x;
        this.motZ = z;
        double closest = dataAssessments.keySet().stream().mapToDouble(d -> d).min().orElse(-1.0);
        Pair pair = motions = closest == -1.0 ? null : (Pair)dataAssessments.get(closest);
        if (motions != null) {
            this.moveStrafe = ((Float)motions.getX()).floatValue();
            this.moveForward = ((Float)motions.getY()).floatValue();
        }
    }

    protected final AxisAlignedBB getEntityBoundingBox() {
        return this.boundingBox;
    }

    public abstract void moveFlying(float var1, float var2, float var3);

    public abstract void moveEntityWithHeading();

    public abstract void moveEntity(double var1, double var3, double var5);

    public static AbstractPredictionHandler loadPredictionHandler(KarhuPlayer data, Karhu karhu) {
        AbstractPredictionHandler p = null;
        if (data.getClientVersion() != null) {
            p = data.isNewerThan8() ? new PredictionHandler1_9(data, karhu) : new PredictionHandler(data, karhu);
        }
        if (p == null) {
            p = new PredictionHandler(data, karhu);
        }
        p.init();
        return p;
    }

    public KarhuPlayer getData() {
        return this.data;
    }

    public Karhu getKarhu() {
        return this.karhu;
    }
}
