package ProjectSteamCrafting.MillStone;

import ARLib.ARLibRegistry;
import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.holoProjector.itemHoloProjector;
import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.multiblockCore.EntityMultiblockMaster;
import ARLib.network.PacketBlockEntity;
import ARLib.utils.DimensionUtils;
import ARLib.utils.ItemUtils;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import ProjectSteam.Static;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static ProjectSteam.Registry.WOODEN_AXLE;
import static ProjectSteamCrafting.Registry.ENTITY_MILLSTONE;
import static ProjectSteamCrafting.Registry.MILLSTONE;

public class EntityMillStone extends EntityMultiblockMaster implements IMechanicalBlockProvider {

    AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return 600;
        }

        @Override
        public double getInertia(Direction direction) {
            return 500;
        }

        @Override
        public double getTorqueResistance(Direction direction) {
            return MillStoneConfig.INSTANCE.resistance;
        }

        @Override
        public double getTorqueProduced(Direction direction) {
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@Nullable Direction direction) {
            return 1;
        }
    };

    public ItemStackHandler inventory = new ItemStackHandler(18) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public void onContentsChanged(int slot) {
            setChanged();
            sendUpdateTag(null);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            for (MillStoneConfig.MillStoneRecipe i : MillStoneConfig.INSTANCE.recipes) {
                if (ItemUtils.matches(i.inputItem.id, stack) || ItemUtils.matches(i.outputItem.id, stack))
                    return true;
            }
            return false;
        }

    };
    public GuiHandlerBlockEntity guiHandler;

    public EntityMillStone(BlockPos pos, BlockState blockState) {
        super(ENTITY_MILLSTONE.get(), pos, blockState);
        myMechanicalBlock.resetRotationAfterX = 360 * 4;
        super.forwardInteractionToMaster = true;

        guiHandler = new GuiHandlerBlockEntity(this);
        for (guiModulePlayerInventorySlot i : guiModulePlayerInventorySlot.makePlayerHotbarModules(10, 150, 100, 1, 0, guiHandler)) {
            guiHandler.getModules().add(i);
        }
        for (guiModulePlayerInventorySlot i : guiModulePlayerInventorySlot.makePlayerInventoryModules(10, 90, 200, 1, 0, guiHandler)) {
            guiHandler.getModules().add(i);
        }

        int offsetY = 10;
        int offsetX = 10;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 9; x++) {
                guiModuleItemHandlerSlot m = new guiModuleItemHandlerSlot(x + y * 9, inventory, x + y * 9, 0, 1, guiHandler, x * 18 + offsetX, y * 18 + offsetY);
                guiHandler.getModules().add(m);
            }
        }
    }

    @Override
    public void onLoad() {
        if (level.isClientSide) {
            CompoundTag i = new CompoundTag();
            i.putUUID("client_onload", Minecraft.getInstance().player.getUUID());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, i));
        }
        super.onLoad();
    }

    public void placeStructurePreview(){
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        CompoundTag info = new CompoundTag();
        info.putString("dimension",DimensionUtils.getLevelId(level));
        info.putString("selectedMachine", "MillStone");
        info.putInt("y",0);
        BlockPos offset = new BlockPos(0,0,0).relative(facing).relative(facing.getClockWise());
        info.putInt("posX",getBlockPos().getX()+offset.getX());
        info.putInt("posY",getBlockPos().getY());
        info.putInt("posZ",getBlockPos().getZ()+offset.getZ());
        info.putInt("stepX", facing.getStepX());
        info.putInt("stepZ", facing.getStepZ());
        ((itemHoloProjector) ARLibRegistry.ITEM_HOLOPROJECTOR.get()).placeLayer(info);
    }

    public void sendUpdateTag(@Nullable ServerPlayer target) {
        if (target == null) {
            if (level instanceof ServerLevel l) {
                PacketDistributor.sendToPlayersTrackingChunk(l, new ChunkPos(getBlockPos()), PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
            }
        } else {
            PacketDistributor.sendToPlayer(target, PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
        }
    }

    public CompoundTag getUpdateTag() {
        CompoundTag info = new CompoundTag();
        info.put("inventory", inventory.serializeNBT(level.registryAccess()));
        return info;
    }

    public void openGui() {
        if (level.isClientSide) {
            guiHandler.openGui(180, 180, true);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer sp) {
            CompoundTag t = new CompoundTag();
            t.putBoolean("openGui", true);
            PacketDistributor.sendToPlayer(sp, PacketBlockEntity.getBlockEntityPacket(this, t));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public void onStructureComplete() {
        if (level.isClientSide)
            // this is executed before minecraft updates the blockstate on client
            // but resetRotation (to make it sync to the rotation) checks for connected mechanical blocks and it only connects to other mechanical blocks when the multiblock is formed
            // so i update it directly here
            level.setBlock(getBlockPos(), getBlockState().setValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED, true), 3);
        myMechanicalBlock.mechanicalOnload();
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();
        if (!level.isClientSide) {
            guiHandler.serverTick();
            if (getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
                double directionMultiplier = 1;
                Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (facing == Direction.EAST || facing == Direction.NORTH)
                    directionMultiplier = -1;

                float directionOffset = 0;
                if (facing == Direction.NORTH) directionOffset = 90;
                if (facing == Direction.SOUTH) directionOffset = 270;
                if (facing == Direction.EAST) directionOffset = 360;
                if (facing == Direction.WEST) directionOffset = 180;

                for (int i = 0; i < 18; i++) {
                    double slotRotation = directionOffset + 20 * i - (float) (directionMultiplier * myMechanicalBlock.currentRotation * 0.25);
                    slotRotation = slotRotation % 360;
                    double lastSlotRotation = directionOffset + 20 * i - (float) ((directionMultiplier * (myMechanicalBlock.currentRotation - Static.rad_to_degree(myMechanicalBlock.internalVelocity) / Static.TPS)) * 0.25);
                    lastSlotRotation = lastSlotRotation % 360;

                    if (Math.abs(slotRotation - lastSlotRotation) > Math.abs(Static.rad_to_degree(myMechanicalBlock.internalVelocity) / Static.TPS) * 2) {
                        ItemStack stackInSlot = inventory.getStackInSlot(i);
                        if (!stackInSlot.isEmpty()) {
                            for (MillStoneConfig.MillStoneRecipe r : MillStoneConfig.INSTANCE.recipes) {
                                if (ItemUtils.matches(r.inputItem.id, stackInSlot)) {
                                    if (level.random.nextFloat() <= 1 / r.timeRequired) {
                                        ItemStack output = ItemUtils.getItemStackFromIdOrTag(r.outputItem.id, r.outputItem.amount, level.registryAccess());
                                        inventory.setStackInSlot(i, output);
                                        setChanged();
                                        sendUpdateTag(null);
                                    }
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        }
        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            if (level.getGameTime() % 51 == 0) {
                super.scanStructure();
            }
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityMillStone) t).tick();
    }

    // "c" is ALWAYS used for the controller/master block.
    public static Object[][][] structure = {
            {{'S', 'A', 'S'}, {'S', 'c', 'S'}, {'S', 'A', 'S'}}
    };
    public static boolean[][][] hideBlocks = {
            {{true, false, true}, {true, true, true}, {true, false, true}}
    };

    public static HashMap<Character, List<Block>> charMapping = new HashMap<>();

    static {
        List<Block> c = new ArrayList<>();
        c.add(MILLSTONE.get());
        charMapping.put('c', c);

        List<Block> A = new ArrayList<>();
        A.add(WOODEN_AXLE.get());
        charMapping.put('A', A);

        List<Block> S = new ArrayList<>();
        S.add(Blocks.STONE_SLAB);
        charMapping.put('S', S);
    }

    public boolean[][][] hideBlocks() {
        return hideBlocks;
    }

    @Override
    public Object[][][] getStructure() {
        return structure;
    }

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return charMapping;
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction direction) {
        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) return null;
        if (direction.getAxis() == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis())
            return myMechanicalBlock;
        else return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        myMechanicalBlock.mechanicalLoadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        myMechanicalBlock.mechanicalSaveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    public void readClient(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadClient(tag);
        guiHandler.readClient(tag);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(level.registryAccess(), tag.getCompound("inventory"));
        }
        if (tag.contains("openGui")) {
            openGui();
        }
        super.readClient(tag);
    }

    @Override
    public void readServer(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadServer(tag);
        guiHandler.readServer(tag);
        if (tag.contains("client_onload")) {
            UUID from = tag.getUUID("client_onload");
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            if (p instanceof ServerPlayer sp) {
                sendUpdateTag(sp);
            }
        }
        super.readServer(tag);
    }
}
