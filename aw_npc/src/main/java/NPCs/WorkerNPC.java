package NPCs;

import NPCs.programs.CropFarmingProgram;
import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
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

    public ItemStackHandler combinedInventory = new ItemStackHandler(0) {

        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot == 0)
                setItemInHand(InteractionHand.MAIN_HAND, stack);
            else if (slot == 1)
                setItemInHand(InteractionHand.OFF_HAND, stack);
            else {
                inventory.setStackInSlot(slot - 2, stack);
            }
        }

        public int getSlots() {
            return 2 + inventory.getSlots();
        }

        public ItemStack getStackInSlot(int slot) {
            if (slot == 0)
                return getMainHandItem();
            else if (slot == 1)
                return getOffhandItem();
            else {
                return inventory.getStackInSlot(slot - 2);
            }
        }

        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot >= 2) return inventory.insertItem(slot - 2, stack, simulate);

            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                ItemStack existing = getStackInSlot(slot);
                int limit = this.getStackLimit(slot, stack);
                if (!existing.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                        return stack;
                    }
                    limit -= existing.getCount();
                }

                if (limit <= 0) {
                    return stack;
                } else {
                    boolean reachedLimit = stack.getCount() > limit;
                    if (!simulate) {
                        if (existing.isEmpty()) {
                            setStackInSlot(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
                        } else {
                            existing.grow(reachedLimit ? limit : stack.getCount());
                        }

                        this.onContentsChanged(slot);
                    }

                    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
                }
            }
        }

        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= 2) return inventory.extractItem(slot - 2, amount, simulate);

            if (amount == 0) {
                return ItemStack.EMPTY;
            } else {
                this.validateSlotIndex(slot);
                ItemStack existing = getStackInSlot(slot);
                if (existing.isEmpty()) {
                    return ItemStack.EMPTY;
                } else {
                    int toExtract = Math.min(amount, existing.getMaxStackSize());
                    if (existing.getCount() <= toExtract) {
                        if (!simulate) {
                            setStackInSlot(slot, ItemStack.EMPTY);
                            this.onContentsChanged(slot);
                            return existing;
                        } else {
                            return existing.copy();
                        }
                    } else {
                        if (!simulate) {
                            setStackInSlot(slot, existing.copyWithCount(existing.getCount() - toExtract));
                            this.onContentsChanged(slot);
                        }

                        return existing.copyWithCount(toExtract);
                    }
                }
            }
        }

        protected void validateSlotIndex(int slot) {
        }
    };

    protected WorkerNPC(EntityType<WorkerNPC> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.noCulling = true;
        setGuaranteedDrop(EquipmentSlot.MAINHAND);
        setDropChance(EquipmentSlot.OFFHAND, 0); // this Stack will only hold reference to stacks in the inventory
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

    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            level.addFreshEntity(new ItemEntity(level,getPosition(0).x,getPosition(0).y,getPosition(0).z, inventory.getStackInSlot(i)));
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

    int failTimeOut = 0;
    HashSet<BlockPos> unreachableBlocks = new HashSet<>();
BlockPos lastTarget = null;
    public ExitCode moveToPosition(BlockPos p, int precision) {
        if (p == null) return ExitCode.EXIT_FAIL;
        if(!p.equals(lastTarget)){
            lastTarget = p;
            failTimeOut = 0;
        }
        if (unreachableBlocks.contains(p)){
            //return ExitCode.EXIT_FAIL;
        }
        int precisionSqr = precision * precision;

        if (getNavigation().getPath() != null && ProgramUtils.distanceToSqr(p, this) <= precisionSqr) {
            return ExitCode.EXIT_SUCCESS;
        }

        if (getNavigation().getPath() == null || getNavigation().getPath().getTarget().getCenter().distanceToSqr(p.getCenter()) > precisionSqr || getNavigation().isStuck() || getNavigation().isDone()) {
            failTimeOut++;
            if (failTimeOut > 1) {
                failTimeOut = 0;
                unreachableBlocks.add(p);
                return ExitCode.EXIT_FAIL;
            }
            Path currentPath = getNavigation().createPath(p, precision);
            getNavigation().moveTo(currentPath, 1);
        } else {
            failTimeOut = 0;
        }
        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
