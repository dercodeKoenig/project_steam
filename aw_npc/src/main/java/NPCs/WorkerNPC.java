package NPCs;

import NPCs.programs.CropFarmingProgram;
import NPCs.programs.ExitCode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WorkerNPC extends PathfinderMob {

    public enum WorkTypes {
        FARMER,
        FISHER,
        MINER,
        HUNTER,
        LUMBERJACK,
        UNEMPLOYED
    }

    public WorkTypes worktype = WorkTypes.UNEMPLOYED;

    public ItemStackHandler inventory = new ItemStackHandler(8);

    protected WorkerNPC(EntityType<WorkerNPC> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.noCulling = true;
        setGuaranteedDrop(EquipmentSlot.MAINHAND);
        setGuaranteedDrop(EquipmentSlot.OFFHAND);
        setGuaranteedDrop(EquipmentSlot.CHEST);
        setGuaranteedDrop(EquipmentSlot.HEAD);
        setGuaranteedDrop(EquipmentSlot.LEGS);
        setGuaranteedDrop(EquipmentSlot.FEET);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes() // Base attributes for mobs
                .add(Attributes.MAX_HEALTH, 20.0D) // Default health
                .add(Attributes.MOVEMENT_SPEED, 0.25D) // Default movement speed
                .add(Attributes.FOLLOW_RANGE, 4096);
    }

    @Override
    protected void registerGoals() {
        List<Goal> activeGoals = new ArrayList<>(goalSelector.getAvailableGoals());
        for (Goal i : activeGoals) {
            goalSelector.removeGoal(i);
        }

        int priority = 0;

        if (worktype != WorkTypes.UNEMPLOYED) {

            if (worktype == WorkTypes.FARMER) {
                this.goalSelector.addGoal(priority++, new CropFarmingProgram(this));

            }
            // empty inventory if > 50% full

            // try farm

            // try empty all inventory (extend empty inventory if > 50% full)

            // if all above fail because current farm is not valid, select a new farm to work on


            //this.goalSelector.addGoal(priority++, new CropFarmingProgram(this));

        }


        this.goalSelector.addGoal(priority++, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(priority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }


    @Override
    public void checkDespawn() {
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {

        if (worktype == WorkTypes.UNEMPLOYED)
            worktype = WorkTypes.FARMER;
        else
            worktype = WorkTypes.UNEMPLOYED;

        registerGoals();
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSwingTime(); //wtf do i need to call this myself??

        if (!level().isClientSide) {
            // slowly forget unreachable blocks
            if (level().getGameTime() % 100 == 0 && !unreachableBlocks.isEmpty()) {
                unreachableBlocks.remove(unreachableBlocks.iterator().next());
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        inventory.deserializeNBT(this.registryAccess(), compound.getCompound("inventory1"));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        compound.put("inventory1", inventory.serializeNBT(this.registryAccess()));
    }


    public double distanceToSqr(BlockPos target) {
        return getPosition(0).distanceToSqr(target.getCenter());
    }

    int ticksWithoutMove = 0;
    HashSet<BlockPos> unreachableBlocks = new HashSet<>();

    public ExitCode moveToPosition(BlockPos p, int precision) {
        if (p == null) return ExitCode.EXIT_FAIL;
        if (unreachableBlocks.contains(p)) return ExitCode.EXIT_FAIL;

        int precisionSqr = precision * precision;

        if (getNavigation().getPath() != null && distanceToSqr(p) <= precisionSqr) {
            return ExitCode.EXIT_SUCCESS;
        }

        if (getNavigation().getPath() == null || getNavigation().getPath().getTarget().getCenter().distanceToSqr(p.getCenter()) > precisionSqr || getNavigation().isStuck() || getNavigation().isDone()) {
            ticksWithoutMove++;
            if (ticksWithoutMove > 1) {
                ticksWithoutMove = 0;
                unreachableBlocks.add(p);
                return ExitCode.EXIT_FAIL;
            }
            Path currentPath = getNavigation().createPath(p, precision);
            getNavigation().moveTo(currentPath, 1);
        } else {
            ticksWithoutMove = 0;
        }
        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
