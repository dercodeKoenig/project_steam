package NPCs;

import NPCs.programs.CropFarming.MainFarmingProgram;
import NPCs.programs.FollowOwnerProgram;
import NPCs.programs.FoodProgramWorker;
import NPCs.programs.ProgramUtils;
import NPCs.programs.SleepProgram;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WorkerNPC extends NPCBase {

    public enum WorkTypes {
        Farmer,
        FISHER,
        MINER,
        HUNTER,
        LUMBERJACK,
        Worker
    }

    public BlockPos lastWorksitePosition;
    public WorkTypes worktype;

    public double cachedDistanceManhattanToWorksite;

    public MainFarmingProgram farmingProgram;
    public FoodProgramWorker foodProgram;

    protected WorkerNPC(EntityType<WorkerNPC> entityType, Level level) {
        super(entityType, level);

        worktype = WorkTypes.Farmer;
        registerGoals();

        setCustomNameVisible(true);

    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes() // Base attributes for mobs
                .add(Attributes.MAX_HEALTH, 20.0D) // Default health
                .add(Attributes.MOVEMENT_SPEED, 0.25D) // Default movement speed
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    protected void registerGoals() {
        List<Goal> activeGoals = new ArrayList<>(goalSelector.getAvailableGoals());
        for (Goal i : activeGoals) {
            goalSelector.removeGoal(i);
        }

        int priority = 0;

        goalSelector.addGoal(priority++, new FollowOwnerProgram(this));

        goalSelector.addGoal(priority++, new SleepProgram(this));

        foodProgram = new FoodProgramWorker(this);
        this.goalSelector.addGoal(priority++, foodProgram);

        goalSelector.addGoal(priority++ ,new OpenDoorGoal(this, true));

        if (worktype == WorkTypes.Farmer) {
            farmingProgram = new MainFarmingProgram(this);
            this.goalSelector.addGoal(priority++, farmingProgram);
        }

        this.goalSelector.addGoal(priority++, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(priority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }


    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (lastWorksitePosition != null)
                cachedDistanceManhattanToWorksite = ProgramUtils.distanceManhattan(this, lastWorksitePosition.getCenter());
            else {
                cachedDistanceManhattanToWorksite = -1;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (lastWorksitePosition != null) {
            compound.putInt("worksitePositionX", lastWorksitePosition.getX());
            compound.putInt("worksitePositionY", lastWorksitePosition.getY());
            compound.putInt("worksitePositionZ", lastWorksitePosition.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("worksitePositionX") && compound.contains("worksitePositionY") && compound.contains("worksitePositionZ")) {
            lastWorksitePosition = new BlockPos(compound.getInt("worksitePositionX"), compound.getInt("worksitePositionY"), compound.getInt("worksitePositionZ"));
        }
    }
}
