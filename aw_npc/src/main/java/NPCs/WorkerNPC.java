package NPCs;

import ARLib.gui.modules.guiModuleItemHandlerSlot;
import NPCs.programs.*;
import NPCs.Items.ItemFoodOrder;
import NPCs.programs.CropFarming.MainFarmingProgram;
import NPCs.programs.Mining.MainMiningProgram;
import NPCs.programs.TreeFarming.MainLumberjackProgram;
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
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;


public class WorkerNPC extends NPCBase {

    public static EntityDataAccessor<Integer> DATA_WORKTYPE = SynchedEntityData.defineId(WorkerNPC.class, EntityDataSerializers.INT);
    public static EntityDataAccessor<String> DATA_TEXTURE = SynchedEntityData.defineId(WorkerNPC.class, EntityDataSerializers.STRING);


    public enum WorkTypes {
        Farmer,
        FISHER,
        Miner,
        HUNTER,
        Lumberjack,
        Worker
    }

    public BlockPos lastWorksitePosition;
    public double cachedDistanceManhattanToWorksite;


    protected WorkerNPC(EntityType<WorkerNPC> entityType, Level level) {
        super(entityType, level);
        //guiModuleItemHandlerSlot workOrderSlot = new guiModuleItemHandlerSlot(13002, ordersStackHandler, 1, 1,0,guiHandler, 140,70);
        //guiHandler.getModules().addFirst(workOrderSlot);
    }

    public MainFarmingProgram farmingProgram;
    public MainLumberjackProgram lumberjackProgram;
    public MainMiningProgram miningProgram;

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes() // Base attributes for mobs
                .add(Attributes.MAX_HEALTH, 20.0D) // Default health
                .add(Attributes.MOVEMENT_SPEED, 0.25D) // Default movement speed
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!level().isClientSide) {
            registerGoals();
            if (getEntityData().get(WorkerNPC.DATA_TEXTURE).isEmpty()) {
                getEntityData().set(WorkerNPC.DATA_TEXTURE, "worker.png");
            }
        }
    }

    @Override
    protected void registerGoals() {
        List<WrappedGoal> activeGoals = new ArrayList<>(goalSelector.getAvailableGoals());
        for (WrappedGoal i : activeGoals) {
            if (i.isRunning())
                i.stop();
        }
        goalSelector.getAvailableGoals().clear();

        farmingProgram = new MainFarmingProgram(this);
        lumberjackProgram = new MainLumberjackProgram(this);
        miningProgram = new MainMiningProgram(this);

        int priority = 0;

        goalSelector.addGoal(priority++, new FollowOwnerProgram(this));

        goalSelector.addGoal(priority++, new SleepProgram(this));

        this.goalSelector.addGoal(priority++, new FoodProgramWorker(this));

        goalSelector.addGoal(priority++, new OpenDoorGoal(this, true));

        //goalSelector.addGoal(priority++ ,new PickupItemsOnGroundProgram(this));

        if (getEntityData().get(DATA_WORKTYPE) == WorkTypes.Farmer.ordinal()) {
            this.goalSelector.addGoal(priority++, farmingProgram);
        }
        if (getEntityData().get(DATA_WORKTYPE) == WorkTypes.Miner.ordinal()) {
            this.goalSelector.addGoal(priority++, miningProgram);
        }
        if (getEntityData().get(DATA_WORKTYPE) == WorkTypes.Lumberjack.ordinal()) {
            this.goalSelector.addGoal(priority++, lumberjackProgram);
        }

        this.goalSelector.addGoal(priority++, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(priority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WORKTYPE, WorkTypes.Worker.ordinal());
        builder.define(DATA_TEXTURE, "");
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            if (player.getItemInHand(hand).getItem().equals(Items.WOODEN_HOE)) {
                getEntityData().set(DATA_WORKTYPE, WorkTypes.Farmer.ordinal());
                int randomNumber = Math.abs(level().random.nextInt()) % 7 + 1;
                getEntityData().set(DATA_TEXTURE, "po_worker_farmer_" + randomNumber + ".png");
                player.setItemInHand(hand, ItemStack.EMPTY);
                registerGoals();
                return InteractionResult.SUCCESS;
            }

            if (player.getItemInHand(hand).getItem().equals(Items.WOODEN_PICKAXE)) {
                getEntityData().set(DATA_WORKTYPE, WorkTypes.Miner.ordinal());
                int randomNumber = Math.abs(level().random.nextInt()) % 5 + 1;
                getEntityData().set(DATA_TEXTURE, "po_worker_miner_" + randomNumber + ".png");
                player.setItemInHand(hand, ItemStack.EMPTY);
                registerGoals();
                return InteractionResult.SUCCESS;
            }

            if (player.getItemInHand(hand).getItem().equals(Items.WOODEN_AXE)) {
                getEntityData().set(DATA_WORKTYPE, WorkTypes.Lumberjack.ordinal());
                int randomNumber = Math.abs(level().random.nextInt()) % 2 + 1;
                getEntityData().set(DATA_TEXTURE, "po_worker_lumberjack_" + randomNumber + ".png");
                player.setItemInHand(hand, ItemStack.EMPTY);
                registerGoals();
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (lastWorksitePosition != null)
                cachedDistanceManhattanToWorksite = Utils.distanceManhattan(this, lastWorksitePosition.getCenter());
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
        compound.putString("texture", getEntityData().get(DATA_TEXTURE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("worksitePositionX") && compound.contains("worksitePositionY") && compound.contains("worksitePositionZ")) {
            lastWorksitePosition = new BlockPos(compound.getInt("worksitePositionX"), compound.getInt("worksitePositionY"), compound.getInt("worksitePositionZ"));
        }
        getEntityData().set(DATA_WORKTYPE, compound.getInt("worktyoe"));
        getEntityData().set(DATA_TEXTURE, compound.getString("texture"));
        registerGoals();
    }
}
