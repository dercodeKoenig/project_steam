package NPCs;

import NPCs.programs.CropFarming.MainFarmingProgram;
import NPCs.programs.FollowOwnerProgram;
import NPCs.programs.FoodProgramWorker;
import NPCs.programs.ProgramUtils;
import NPCs.programs.SleepProgram;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WorkerNPC extends NPCBase {

    public static EntityDataAccessor<Integer> DATA_WORKTYPE = SynchedEntityData.defineId(WorkerNPC.class,EntityDataSerializers.INT);

    public enum WorkTypes {
        Farmer,
        FISHER,
        MINER,
        HUNTER,
        LUMBERJACK,
        Worker
    }

    public BlockPos lastWorksitePosition;

    public double cachedDistanceManhattanToWorksite;

    protected WorkerNPC(EntityType<WorkerNPC> entityType, Level level) {
        super(entityType, level);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes() // Base attributes for mobs
                .add(Attributes.MAX_HEALTH, 20.0D) // Default health
                .add(Attributes.MOVEMENT_SPEED, 0.25D) // Default movement speed
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        registerGoals();
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

        this.goalSelector.addGoal(priority++, new FoodProgramWorker(this));

        goalSelector.addGoal(priority++ ,new OpenDoorGoal(this, true));

        if (getEntityData().get(DATA_WORKTYPE) == WorkTypes.Farmer.ordinal()) {
            this.goalSelector.addGoal(priority++, new MainFarmingProgram(this));
        }

        this.goalSelector.addGoal(priority++, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(priority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WORKTYPE, WorkTypes.Worker.ordinal());
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(!level().isClientSide) {
            if (player.getItemInHand(hand).getItem().equals(Items.WOODEN_HOE)) {
                getEntityData().set(DATA_WORKTYPE,WorkTypes.Farmer.ordinal());
                player.setItemInHand(hand, ItemStack.EMPTY);
                registerGoals();
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player,hand);
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

        compound.putInt("worktyoe", getEntityData().get(DATA_WORKTYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("worksitePositionX") && compound.contains("worksitePositionY") && compound.contains("worksitePositionZ")) {
            lastWorksitePosition = new BlockPos(compound.getInt("worksitePositionX"), compound.getInt("worksitePositionY"), compound.getInt("worksitePositionZ"));
        }
        getEntityData().set(DATA_WORKTYPE, compound.getInt("worktyoe"));
    }
}
