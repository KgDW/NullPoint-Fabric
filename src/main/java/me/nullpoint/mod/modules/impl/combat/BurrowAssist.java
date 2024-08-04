package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.combat.MeteorExplosionUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BurrowAssist extends Module {
    public static BurrowAssist INSTANCE;
    public static Timer delay = new Timer();
    private final SliderSetting Delay =
            add(new SliderSetting("Delay", 100, 0, 1000));
    public BooleanSetting pause=add(new BooleanSetting("PauseEat",true));
    public SliderSetting speed = add(new SliderSetting("MaxSpeed", 8, 0, 20));
    public BooleanSetting ccheck = add(new BooleanSetting("CheckCrystal", true).setParent());
    private final SliderSetting cRange =
            add(new SliderSetting("Range", 5.0, 0.0, 6.0, v -> ccheck.isOpen()));
    private final SliderSetting breakMinSelf =
            add(new SliderSetting("BreakSelf", 12.0, 0.0, 36.0, v -> ccheck.isOpen()));
    public BooleanSetting mcheck = add(new BooleanSetting("CheckMine", true).setParent());
    public BooleanSetting mself = add(new BooleanSetting("Self", true, v -> mcheck.isOpen()));
    private final SliderSetting predictTicks =
            add(new SliderSetting("PredictTicks", 4, 0, 10));
    private final BooleanSetting terrainIgnore =
            add(new BooleanSetting("TerrainIgnore", true));
    public BurrowAssist() {
        super("BurrowAssist",Category.Combat);
        INSTANCE = this;
    }
    public final HashMap<PlayerEntity, Double> playerSpeeds = new HashMap();
    @Override
    public void onUpdate() {
        if(nullCheck()){
            return;
        }
        if(!delay.passed((long) Delay.getValue())) return;
        if(pause.getValue() && mc.player.isUsingItem()){
            return;
        }
        if(mc.options.jumpKey.isPressed()){
            return;
        }
        if (!canbur()){
            return;
        }
        if(mc.player.isOnGround() &&
                getPlayerSpeed(mc.player) < speed.getValueInt() &&
                (ccheck.getValue() && mcheck.getValue() ? (findcrystal() || checkmine(mself.getValue())) : ((!ccheck.getValue() || findcrystal()) && (!mcheck.getValue() || checkmine(mself.getValue()))))){

            if(Burrow.INSTANCE.isOn()) return;

            Burrow.INSTANCE.enable();

            delay.reset();
        }

    }

    public boolean findcrystal(){
        PlayerAndPredict self = new PlayerAndPredict(mc.player);
        for (Entity crystal : mc.world.getEntities()) {
            if (!(crystal instanceof EndCrystalEntity)) continue;
            if (EntityUtil.getEyesPos().distanceTo(crystal.getPos()) > cRange.getValue()) continue;
            float selfDamage = calculateDamage(crystal.getPos(), self.player, self.predict);
            if (selfDamage < breakMinSelf.getValue()) continue;
            return true;
        }
        return false;
    }

    public double getPlayerSpeed(PlayerEntity player) {
        if (playerSpeeds.get(player) == null) {
            return 0.0;
        }
        return turnIntoKpH(playerSpeeds.get(player));
    }

    public double turnIntoKpH(double input) {
        return (double) MathHelper.sqrt((float) input) * 71.2729367892;
    }

    public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
        if (terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        float damage = 0;
        damage = (float) MeteorExplosionUtil.crystalDamage(player, pos, predict);
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    public boolean checkmine(boolean self){
        ArrayList<BlockPos> pos = new ArrayList<>();
        pos.add(EntityUtil.getPlayerPos(true));
        pos.add(new BlockPosX(mc.player.getX() + 0.4, mc.player.getY() + 0.5, mc.player.getZ() + 0.4));
        pos.add(new BlockPosX(mc.player.getX() - 0.4, mc.player.getY() + 0.5, mc.player.getZ() + 0.4));
        pos.add(new BlockPosX(mc.player.getX() + 0.4, mc.player.getY() + 0.5, mc.player.getZ() - 0.4));
        pos.add(new BlockPosX(mc.player.getX() - 0.4, mc.player.getY() + 0.5, mc.player.getZ() - 0.4));
        for (MineManager.BreakData breakData : new HashMap<>(Nullpoint.BREAK.breakMap).values()) {
            if (breakData == null || breakData.getEntity() == null) continue;
            for (BlockPos pos1 : pos){
                if(pos1.equals(breakData.pos) && breakData.getEntity() != mc.player){
                    return true;
                }
            }
        }
        if(!self){
            return false;
        }
        for (BlockPos pos1 : pos){
            if(pos1.equals(SpeedMine.breakPos)){
                return true;
            }
        }
        return false;
    }

    public class PlayerAndPredict {
        PlayerEntity player;
        PlayerEntity predict;
        public PlayerAndPredict(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValueFloat() > 0) {
                predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {@Override public boolean isSpectator() {return false;} @Override public boolean isCreative() {return false;}};
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : player.getStatusEffects()) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }

    private static boolean canbur(){
        BlockPos pos1 = new BlockPosX(mc.player.getX() + 0.3, mc.player.getY() + 0.5, mc.player.getZ() + 0.3);
        BlockPos pos2 = new BlockPosX(mc.player.getX() - 0.3, mc.player.getY() + 0.5, mc.player.getZ() + 0.3);
        BlockPos pos3 = new BlockPosX(mc.player.getX() + 0.3, mc.player.getY() + 0.5, mc.player.getZ() - 0.3);
        BlockPos pos4 = new BlockPosX(mc.player.getX() - 0.3, mc.player.getY() + 0.5, mc.player.getZ() - 0.3);
        BlockPos playerPos = EntityUtil.getPlayerPos(true);
        return Burrow.INSTANCE.canPlace(pos1) || Burrow.INSTANCE.canPlace(pos2) || Burrow.INSTANCE.canPlace(pos3) || Burrow.INSTANCE.canPlace(pos4);
    }
}
