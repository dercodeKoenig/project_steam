package Farms.CropFarm;

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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

import static Farms.Registry.ENTITY_CROP_FARM;

public class EntityCropFarm extends EntityFarmBase {

    int energy_plant = 3000;
    int energy_harvest = 3000;
    int energy_boneMeal = 2000;

    ItemStackHandler mainInventory = new ItemStackHandler(18) {
        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    ItemStackHandler inputsInventory = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isItemValidSeed(stack);
        }

        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    ItemStackHandler specialResourcesInventory = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.getItem().equals(Items.BONE_MEAL))
                return true;
            else return false;
        }

        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    //public Set<BlockPos> positionsToTill = new HashSet<>();
    public Set<BlockPos> positionsToPlant = new HashSet<>();
    public Set<BlockPos> positionsToHarvest = new HashSet<>();
    public Set<BlockPos> positionsToBoneMeal = new HashSet<>();
    int currentBlockToScanIndex = 0;

    public EntityCropFarm(BlockPos pos, BlockState blockState) {
        super(ENTITY_CROP_FARM.get(), pos, blockState);

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

    public boolean isItemValidSeed(ItemStack s) {
        if (s.getItem() instanceof BlockItem bi) {
            Block itemBlock = bi.getBlock();
            if (itemBlock instanceof CropBlock) {
                return true;
            }
            if (itemBlock instanceof StemBlock) {
                return true;
            }
            if (itemBlock.equals(Blocks.NETHER_WART)) {
                return true;
            }
            if (itemBlock.equals(Blocks.SUGAR_CANE)) {
                return true;
            }
            if (itemBlock.equals(Blocks.CACTUS)) {
                return true;
            }
        }
        return false;
    }

    public boolean canPlant(BlockPos p) {
        BlockState state = level.getBlockState(p);
        if (state.isAir()) return true;
        if (state.canBeReplaced()) return true;
        return false;
    }
    public boolean tryPlantPosition(BlockPos p) {
        if (!canPlant(p)) return false;

        //System.out.println("try plant: "+p);

        //if a stemBlock is around (melon/pumpkin) do not plant next to it
        for (int z = -1; z <= 1; z++) {
            if (level.getBlockState(p.offset(0, 0, z)).getBlock() instanceof StemBlock||
                    level.getBlockState(p.offset(0, 0, z)).getBlock() instanceof AttachedStemBlock) {
                return false;
            }
        }
        for (int x = -1; x <= 1; x++) {
            if (level.getBlockState(p.offset(x, 0, 0)).getBlock() instanceof StemBlock||
                    level.getBlockState(p.offset(x, 0, 0)).getBlock() instanceof AttachedStemBlock) {
                return false;
            }
        }

        for (int i = 0; i < inputsInventory.getSlots(); i++) {
            ItemStack s = inputsInventory.getStackInSlot(i);
            if (!s.isEmpty() && s.getItem() instanceof BlockItem bi) {
                if (bi.getBlock().defaultBlockState().canSurvive(level, p)) {
                    level.setBlock(p, bi.getBlock().defaultBlockState(), 3);
                    s.shrink(1);
                    return true;
                }
            }
        }

        // if no block was planted, try till dirt instead
        BlockState s = level.getBlockState(p.below());
        Block b = s.getBlock();
        if(b.equals(Blocks.DIRT) || b.equals(Blocks.DIRT_PATH) || b.equals(Blocks.GRASS_BLOCK)){
            level.setBlock(p.below(), Blocks.FARMLAND.defaultBlockState(), 3);
            return true;
        }

        return false;
    }

    public boolean canBoneMeal(BlockPos p){
        BlockState state = level.getBlockState(p);
        return state.getBlock() instanceof BonemealableBlock bab && bab.isValidBonemealTarget(level,p,state);
    }
    public boolean canHarvestPosition(BlockPos p) {
        BlockState state = level.getBlockState(p);
        if (state.getBlock() instanceof CropBlock cp && cp.isMaxAge(state))
            return true;
        if (state.getBlock() instanceof NetherWartBlock wp && state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE)
            return true;

        if (state.getBlock().equals(Blocks.PUMPKIN) ||
                state.getBlock().equals(Blocks.MELON) ||
                state.getBlock().equals(Blocks.SUGAR_CANE) ||
                state.getBlock().equals(Blocks.CACTUS)) {
            return true;
        }
        return false;
    }

    public BlockPos getPositionToHarvest(BlockPos p) {
        BlockState state = level.getBlockState(p);
        if (state.getBlock() instanceof CropBlock cp && cp.isMaxAge(state))
            return p;
        if (state.getBlock() instanceof NetherWartBlock wp && state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE)
            return p;
        if (state.getBlock().equals(Blocks.PUMPKIN))
            return p;
        if (state.getBlock().equals(Blocks.MELON))
            return p;

        if (state.getBlock().equals(Blocks.SUGAR_CANE) || state.getBlock().equals(Blocks.CACTUS)) {
            int i = 0;
            while (i < 10) {
                if (canHarvestPosition(p.relative(Direction.UP,i+1))) i++;
                else break;
            }
            if (i > 0) return p.relative(Direction.UP, i);
        }
        return null;
    }
    public boolean harvestPosition(BlockPos p) {
        if (canHarvestPosition(p)) {
            BlockState s = level.getBlockState(p);
            LootParams.Builder b = new LootParams.Builder((ServerLevel) level)
                    .withParameter(LootContextParams.TOOL,new ItemStack(Items.IRON_HOE))
                    .withParameter(LootContextParams.ORIGIN,getBlockPos().getCenter());
            List<ItemStack> drops = s.getDrops(b);

            //this does not consider that inputsInventory only can hold valid seeds
            // worst case some items get "voided" if mainInventory is full but inputsInventory is not
            if(InventoryUtils.canInsertAllItems(List.of(inputsInventory,mainInventory),drops)) {
                level.destroyBlock(p,false);
                for (ItemStack i : drops) {
                    if (isItemValidSeed(i)) {
                        for (int j = 0; j < inputsInventory.getSlots(); j++) {
                            i = inputsInventory.insertItem(j,i,false);
                        }
                    }
                    for (int j = 0; j < mainInventory.getSlots(); j++) {
                        i = mainInventory.insertItem(j,i,false);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void scanStep(){
        if (!allowedBlocksList.isEmpty()) {
            if (currentBlockToScanIndex >= allowedBlocksList.size()) {
                currentBlockToScanIndex = 0;
            }
            BlockPos nextPosToScan = allowedBlocksList.get(currentBlockToScanIndex);
            currentBlockToScanIndex += 1;

            if(blackListAsBlockPos.contains(nextPosToScan))
                return;

            if (canPlant(nextPosToScan)) {
                positionsToPlant.add(nextPosToScan);
            }
            if(canBoneMeal(nextPosToScan)){
                positionsToBoneMeal.add(nextPosToScan);
            }
            BlockPos nextHarvestPos = getPositionToHarvest(nextPosToScan);
            //System.out.println(nextHarvestPos+":"+nextPosToScan);
            if(nextHarvestPos != null){
                positionsToHarvest.add(nextHarvestPos);
            }
        }
    }

    public boolean tryPlant(){
        while (!positionsToPlant.isEmpty()) {
            BlockPos target = positionsToPlant.iterator().next();
            positionsToPlant.remove(target);
            if (tryPlantPosition(target)) {
                return true;
            }
        }
        return false;
    }
    public boolean tryHarvest(){
        while (!positionsToHarvest.isEmpty()) {
            BlockPos target = positionsToHarvest.iterator().next();
            positionsToHarvest.remove(target);
            if (harvestPosition(target)) {
                return  true;
            }
        }
        return false;
    }
    public boolean tryBoneMeal(){
        while (!positionsToBoneMeal.isEmpty()) {
            List<BlockPos> shuffledList = new ArrayList<>(positionsToBoneMeal);
            Collections.shuffle(shuffledList);
            BlockPos target = shuffledList.getFirst();
            positionsToBoneMeal.remove(target);

            if (canBoneMeal(target)) {
                for (int i = 0; i < specialResourcesInventory.getSlots(); i++) {
                    ItemStack stackInSlot = specialResourcesInventory.getStackInSlot(i);
                    if(stackInSlot.getItem().equals(Items.BONE_MEAL)){
                        stackInSlot.shrink(1);
                        BlockState s = level.getBlockState(target);
                        if (s.getBlock() instanceof BonemealableBlock bab) {
                            bab.performBonemeal((ServerLevel) level, level.random, target, s);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            scanStep();

            a:
            {
                if (battery.getEnergyStored() > 5000) {
                    if (tryPlant()) {
                        battery.extractEnergy(energy_plant, false);
                        break a;
                    }
                    if (tryHarvest()) {
                        battery.extractEnergy(energy_harvest, false);
                        break a;
                    }
                    if (tryBoneMeal()) {
                        battery.extractEnergy(energy_boneMeal, false);
                        break a;
                    }
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
