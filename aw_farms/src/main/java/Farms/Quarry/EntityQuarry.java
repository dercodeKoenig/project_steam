package Farms.Quarry;

import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.gui.modules.guiModuleText;
import ARLib.utils.InventoryUtils;
import Farms.EntityFarmBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

import static Farms.Registry.ENTITY_FISH_FARM;
import static Farms.Registry.ENTITY_QUARRY;

public class EntityQuarry extends EntityFarmBase {

    public int energy_try_quarry = 8000;
    int yTarget = 0;

    public TreeSet<BlockPos> blocksToMine = new TreeSet<>(new Comparator<BlockPos>() {
        @Override
        // this stupid thing needs always a +/- value or it will just skip adding the entry
        // so first compare y, and if equal compare the others to get some order
        public int compare(BlockPos o1, BlockPos o2) {
            int res = -(o1.getY() - o2.getY());
            if(res == 0)
                res = (o1.getX() - o2.getX());
            if(res == 0)
                res = (o1.getZ() - o2.getZ());
            return res;
        }
    });

    public ItemStackHandler mainInventory = new ItemStackHandler(18) {
        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    public ItemStackHandler inputsInventory = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    public ItemStackHandler specialResourcesInventory = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.getItem().equals(Items.FISHING_ROD))
                return true;
            else return false;
        }

        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    int currentBlockToScanIndex = 0;

    public EntityQuarry(BlockPos pos, BlockState blockState) {
        super(ENTITY_QUARRY.get(), pos, blockState);

        maxSize = 64;

        for (GuiModuleBase m : guiModulePlayerInventorySlot.makePlayerHotbarModules(10, 210, 500, 0, 1, guiHandlerMain)) {
            guiHandlerMain.getModules().add(m);
        }
        for (GuiModuleBase m : guiModulePlayerInventorySlot.makePlayerInventoryModules(10, 150, 600, 0, 1, guiHandlerMain)) {
            guiHandlerMain.getModules().add(m);
        }


        guiModuleText t1 = new guiModuleText(11001, "Output", guiHandlerMain, 10, 30, 0xff000000, false);
        guiHandlerMain.getModules().add(t1);
        guiModuleText t2 = new guiModuleText(11002, "Resources", guiHandlerMain, 10, 78, 0xff000000, false);
        guiHandlerMain.getModules().add(t2);
        guiModuleText t3 = new guiModuleText(11003, "Special Resources", guiHandlerMain, 10, 110, 0xff000000, false);
        guiHandlerMain.getModules().add(t3);

        for (int i = 0; i < inputsInventory.getSlots(); i++) {
            int x = i * 18 + 10;
            int y = 89;
            guiModuleItemHandlerSlot s = new guiModuleItemHandlerSlot(900 + i, inputsInventory, i, 1, 0, guiHandlerMain, x, y);
            guiHandlerMain.getModules().add(s);
        }

        for (int i = 0; i < specialResourcesInventory.getSlots(); i++) {
            int x = i * 18 + 10;
            int y = 120;
            guiModuleItemHandlerSlot s = new guiModuleItemHandlerSlot(1000 + i, specialResourcesInventory, i, 1, 0, guiHandlerMain, x, y);
            guiHandlerMain.getModules().add(s);
        }

        int w = 9;
        for (int i = 0; i < mainInventory.getSlots(); i++) {
            int x = i % w * 18 + 10;
            int y = i / w * 18 + 40;
            guiModuleItemHandlerSlot s = new guiModuleItemHandlerSlot(800 + i, mainInventory, i, 1, 0, guiHandlerMain, x, y);
            guiHandlerMain.getModules().add(s);
        }
    }

    @Override
    public void updateBoundsBp() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos p1 = getBlockPos().relative(facing, controllerOffsetH - 1);
        p1 = p1.relative(facing.getClockWise(), controllerOffsetW);
        p1 = new BlockPos(p1.getX(), yTarget, p1.getZ());
        BlockPos p2 = p1.relative(facing.getCounterClockWise(), w - 1).relative(facing.getOpposite(), h - 1).relative(Direction.UP, getBlockPos().getY()-yTarget);

        pmin = new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
        pmax = new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));

        // this farm does not use blacklist
        updateAllowedBlocksList();
    }
    @Override
    public void updateAllowedBlocksList() {
        // compute allowed blockpos
        allowedBlocks.clear();
        allowedBlocksList.clear();
        for (int z = pmin.getZ(); z <= pmax.getZ(); z++) {
            for (int x = pmin.getX(); x <= pmax.getX(); x++) {
                for (int y = pmin.getY(); y <= pmax.getY(); y++) {
                    BlockPos target = new BlockPos(x, y, z);
                    if (!blackListAsBlockPos.contains(target)) {
                        allowedBlocks.add(target);
                        allowedBlocksList.add(target);
                    }
                }
            }
        }
    }

    public boolean tryQuarry() {
        if (!blocksToMine.isEmpty()) {
            BlockPos target = blocksToMine.getFirst();
            BlockState s = level.getBlockState(target);
            LootParams.Builder b = new LootParams.Builder((ServerLevel) level)
                    .withParameter(LootContextParams.TOOL, new ItemStack(Items.DIAMOND_PICKAXE))
                    .withParameter(LootContextParams.ORIGIN, getBlockPos().getCenter());
            List<ItemStack> drops = s.getDrops(b);

            if (InventoryUtils.canInsertAllItems(List.of(mainInventory), drops)) {
                level.destroyBlock(target, false);
                for (ItemStack i : drops) {
                    for (int j = 0; j < mainInventory.getSlots(); j++) {
                        i = mainInventory.insertItem(j, i, false);
                    }
                }
                blocksToMine.remove(target);
                return true;
            }
        }
        return false;
    }

    public void scanStep() {
        if (!allowedBlocksList.isEmpty()) {
            if (currentBlockToScanIndex >= allowedBlocksList.size()) {
                currentBlockToScanIndex = 0;
            }
            BlockPos nextPosToScan = allowedBlocksList.get(currentBlockToScanIndex);
            currentBlockToScanIndex += 1;

            if (blackListAsBlockPos.contains(nextPosToScan))
                return;

            BlockState s = level.getBlockState(nextPosToScan);
            if (!s.isAir() && s.getFluidState().isEmpty()) {
                blocksToMine.add(nextPosToScan);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            scanStep();

            if (battery.getEnergyStored() > energy_try_quarry) {
                if (tryQuarry()) {
                    battery.extractEnergy(energy_try_quarry, false);
                }
            }
        }
    }

    @Override
    public void openMainGui() {
        if (level.isClientSide) {
            guiHandlerMain.openGui(180, 240, true);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inv1", mainInventory.serializeNBT(registries));
        tag.put("inv2", inputsInventory.serializeNBT(registries));
        tag.put("inv3", specialResourcesInventory.serializeNBT(registries));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mainInventory.deserializeNBT(registries, tag.getCompound("inv1"));
        inputsInventory.deserializeNBT(registries, tag.getCompound("inv2"));
        specialResourcesInventory.deserializeNBT(registries, tag.getCompound("inv3"));
    }
}
