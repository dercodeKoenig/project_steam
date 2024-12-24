package ProjectSteamAW2Generators.StirlingGenerator;

import ARLib.ARLib;
import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ARLib.utils.BlockEntityItemStackHandler;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.checkerframework.checker.units.qual.C;

import java.util.UUID;

import static ProjectSteamAW2Generators.Registry.ENTITY_STIRLING_GENERATOR;
import static ProjectSteamAW2Generators.Registry.ENTITY_WATERWHEEL_GENERATOR;


public class EntityStirlingGenerator extends BlockEntity implements INetworkTagReceiver, IMechanicalBlockProvider {

    public static double maxForceMultiplier = 50;
    public static double k = 10;

    double myFriction = 1;
    double myInertia = 20;
    double maxStress = 2000;
    double myForce = 0;

    IGuiHandler guiHandler;
    BlockEntityItemStackHandler inventory;

    int currentBurnTime;

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return maxStress;
        }

        @Override
        public double getInertia(Direction face) {
            return myInertia;
        }

        @Override
        public double getTorqueResistance(Direction face) {
            return myFriction;
        }

        @Override
        public double getTorqueProduced(Direction face) {
            return myForce;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
            return 1;
        }
    };

    public EntityStirlingGenerator(BlockPos pos, BlockState blockState) {
        super(ENTITY_STIRLING_GENERATOR.get(), pos, blockState);

        guiHandler = new GuiHandlerBlockEntity(this);
        inventory = new BlockEntityItemStackHandler(1,this){
            public boolean isItemValid(int slot, ItemStack stack) {
                if(stack.getItem().getBurnTime(stack, null) >0){
                    return true;
                }
                return false;
            }
        };

        guiModuleItemHandlerSlot s1 = new guiModuleItemHandlerSlot(0,inventory,0,1,0,guiHandler,70,10);
        guiHandler.registerModule(s1);
        for( GuiModuleBase i: guiModulePlayerInventorySlot.makePlayerHotbarModules(10,120,200,0,1,guiHandler)){
            guiHandler.registerModule(i);
        }
        for( GuiModuleBase i: guiModulePlayerInventorySlot.makePlayerInventoryModules(10,50,100,0,1,guiHandler)){
            guiHandler.registerModule(i);
        }
    }

    public void openGui(){
        if(level.isClientSide) {
            guiHandler.openGui(180, 150);
        }
    }

    CompoundTag getUpdateTag(){
        CompoundTag t = new CompoundTag();
        t.putInt("burnTime", currentBurnTime);
        return t;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
        if(level.isClientSide){
            CompoundTag onLoadRequest = new CompoundTag();
            onLoadRequest.putUUID("client_onload", Minecraft.getInstance().player.getUUID());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this,onLoadRequest));
        }
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();
        currentBurnTime --;
        if (!level.isClientSide) {
            IGuiHandler.serverTick(guiHandler);

            if (currentBurnTime <= 0) {
                Item currentBurnItem = inventory.extractItem(0, 1, false).getItem();
                currentBurnTime = currentBurnItem.getBurnTime(new ItemStack(currentBurnItem), null);
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(getBlockPos()), PacketBlockEntity.getBlockEntityPacket(this,getUpdateTag()));
            }
            if (currentBurnTime > 0) {
                float directionMultiplier = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
                myForce = directionMultiplier * maxForceMultiplier - k * myMechanicalBlock.internalVelocity;
            } else {
                myForce = 0;
            }
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityStirlingGenerator) t).tick();
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        myMechanicalBlock.mechanicalReadServer(compoundTag);
        guiHandler.readServer(compoundTag);

        if(compoundTag.contains("client_onload")){
            UUID from = compoundTag.getUUID("client_onload");
            ServerPlayer pfrom = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            if(pfrom != null){
                PacketDistributor.sendToPlayer(pfrom,PacketBlockEntity.getBlockEntityPacket(this,getUpdateTag()));
            }
        }
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        myMechanicalBlock.mechanicalReadClient(compoundTag);
        guiHandler.readClient(compoundTag);

        if(compoundTag.contains("burnTime")){
            currentBurnTime = compoundTag.getInt("burnTime");
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        myMechanicalBlock.mechanicalLoadAdditional(tag, registries);
        inventory.deserializeNBT(registries,tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        myMechanicalBlock.mechanicalSaveAdditional(tag, registries);
        CompoundTag itag =  inventory.serializeNBT(registries);
        tag.put("inventory", itag);
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction direction) {
        if (direction == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            return myMechanicalBlock;
        }
        return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }
}