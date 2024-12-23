package ProjectSteamCrafting.SpinningWheel;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.network.INetworkTagReceiver;
import ARLib.utils.BlockEntityItemStackHandler;
import ARLib.utils.InventoryUtils;
import ARLib.utils.MachineRecipe;
import ARLib.utils.recipePart;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import ProjectSteam.Static;
import ProjectSteamCrafting.Sieve.EntitySieve;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

import static ProjectSteamCrafting.Registry.ENTITY_SPINNING_WHEEL;

public class EntitySpinningWheel extends BlockEntity implements INetworkTagReceiver, IMechanicalBlockProvider {

    public static SpinningWheelConfig config = SpinningWheelConfigLoader.loadConfig();

    public SpinningWheelConfig.MachineRecipe currentRecipe = null;
    public double currentProgress;

    public BlockEntityItemStackHandler inventoryOutput;
    public BlockEntityItemStackHandler inventoryInput;
    List<IItemHandler> itemHandlerInputs = new ArrayList<>();
    List<IItemHandler> itemHandlerOutputs = new ArrayList<>();

    public IGuiHandler guiHandler;

    public int ticksRemainingForForce = 0;
    double myFriction = config.baseResistance;
    double myInertia = 10;
    double maxStress = 100;
    double myForce = 0;

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

    public EntitySpinningWheel(BlockPos pos, BlockState blockState) {
        super(ENTITY_SPINNING_WHEEL.get(), pos, blockState);

        inventoryInput = new BlockEntityItemStackHandler(4,this);
        inventoryOutput = new BlockEntityItemStackHandler(4,this);

        itemHandlerInputs.add(inventoryInput);
        itemHandlerOutputs.add(inventoryOutput);

        guiHandler = new GuiHandlerBlockEntity(this);
        for(guiModulePlayerInventorySlot i : guiModulePlayerInventorySlot.makePlayerHotbarModules(10,130,100,1,0,guiHandler)){
            guiHandler.registerModule(i);
        }
        for(guiModulePlayerInventorySlot i :guiModulePlayerInventorySlot.makePlayerInventoryModules(10,70,200,1,0,guiHandler)){
            guiHandler.registerModule(i);
        }

        guiModuleItemHandlerSlot i1 = new guiModuleItemHandlerSlot(0,inventoryInput,0,0,1,guiHandler,20,10);
        guiHandler.registerModule(i1);
        guiModuleItemHandlerSlot i2 = new guiModuleItemHandlerSlot(1,inventoryInput,1,0,1,guiHandler,20,30);
        guiHandler.registerModule(i2);
        guiModuleItemHandlerSlot i3 = new guiModuleItemHandlerSlot(2,inventoryInput,2,0,1,guiHandler,40,10);
        guiHandler.registerModule(i3);
        guiModuleItemHandlerSlot i4 = new guiModuleItemHandlerSlot(3,inventoryInput,3,0,1,guiHandler,40,30);
        guiHandler.registerModule(i4);

        guiModuleItemHandlerSlot o1 = new guiModuleItemHandlerSlot(4,inventoryOutput,0,2,1,guiHandler,110,10);
        guiHandler.registerModule(o1);
        guiModuleItemHandlerSlot o2 = new guiModuleItemHandlerSlot(5,inventoryOutput,1,2,1,guiHandler,110,30);
        guiHandler.registerModule(o2);
        guiModuleItemHandlerSlot o3 = new guiModuleItemHandlerSlot(6,inventoryOutput,2,2,1,guiHandler,130,10);
        guiHandler.registerModule(o3);
        guiModuleItemHandlerSlot o4 = new guiModuleItemHandlerSlot(7,inventoryOutput,3,2,1,guiHandler,130,30);
        guiHandler.registerModule(o4);
    }

    @Override
    public void onLoad(){
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
    }

    public void resetRecipe() {
        currentRecipe = null;
        currentProgress = 0;
    }

    public void scanFornewRecipe() {
        for (SpinningWheelConfig.MachineRecipe r : config.recipes) {
            if(InventoryUtils.hasInputs(itemHandlerInputs, new ArrayList<>(), List.of(new recipePart(r.inputItem.id,r.inputItem.amount,1)))){
                currentRecipe = r;
                break;
            }
        }
    }

    public boolean tryAddManualWork() {
        if (ticksRemainingForForce < 5 ) {
            ticksRemainingForForce += 5;
            return true;
        }
        return false;
    }
    public InteractionResult use(Player player) {
        if(player.isShiftKeyDown()) {
            if (tryAddManualWork()) {
                player.causeFoodExhaustion(0.2f);
            }
        }
        else{
            if(level.isClientSide)
                guiHandler.openGui(180,160);
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    public void completeCurrentRecipe() {
        for (int i = 0; i < currentRecipe.inputItem.amount; i++) {
            if (level.random.nextFloat() < currentRecipe.inputItem.p) {
                InventoryUtils.consumeElements(new ArrayList<>(), itemHandlerInputs, currentRecipe.inputItem.id, 1, false);
            }
        }
        for (SpinningWheelConfig.MachineRecipe.Item output : currentRecipe.outputItems) {
            for (int i = 0; i < output.amount; i++) {
                if (level.random.nextFloat() < output.p) {
                    InventoryUtils.createElements(new ArrayList<>(), itemHandlerOutputs, output.id, 1);
                }
            }
        }
        resetRecipe();
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();
        if (!level.isClientSide) {
            IGuiHandler.serverTick(guiHandler);

            if (currentRecipe == null) {
                scanFornewRecipe();
            } else {
                if (InventoryUtils.hasInputs(itemHandlerInputs, new ArrayList<>(), List.of(new recipePart(currentRecipe.inputItem.id, currentRecipe.inputItem.amount, 1)))) {
                    double progressMade = Math.abs((float) (Static.rad_to_degree(myMechanicalBlock.internalVelocity) / 360f / Static.TPS));
                    currentProgress += progressMade;
                    if (currentProgress >= currentRecipe.timeRequired) {
                        List<recipePart> myOutputPartsForUtilsLib = new ArrayList<>();
                        for (SpinningWheelConfig.MachineRecipe.Item i : currentRecipe.outputItems) {
                            myOutputPartsForUtilsLib.add(new recipePart(i.id, i.amount, i.p));
                        }
                        if (InventoryUtils.canFitElements(itemHandlerOutputs, new ArrayList<>(), myOutputPartsForUtilsLib)) {
                            completeCurrentRecipe();
                        }
                    }
                } else {
                    resetRecipe();
                }
            }
            if (currentRecipe == null) {
                myFriction = config.baseResistance;
            } else {
                myFriction = config.baseResistance + currentRecipe.additionalResistance;
            }

            if (ticksRemainingForForce > 0) {
                ticksRemainingForForce--;
                myForce = config.clickForce - config.k * myMechanicalBlock.internalVelocity;
            } else {
                myForce = 0;
                ticksRemainingForForce = 0;
            }
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntitySpinningWheel) t).tick();
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        myMechanicalBlock.mechanicalReadServer(compoundTag);
        guiHandler.readServer(compoundTag);
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        myMechanicalBlock.mechanicalReadClient(compoundTag);
        guiHandler.readClient(compoundTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        myMechanicalBlock.mechanicalLoadAdditional(tag, registries);

        inventoryOutput.deserializeNBT(registries,tag.getCompound("inventoryOutput"));
        inventoryInput.deserializeNBT(registries,tag.getCompound("inventoryInput"));
    }
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        myMechanicalBlock.mechanicalSaveAdditional(tag, registries);

        tag.put("inventoryOutput", inventoryOutput.serializeNBT(registries)) ;
        tag.put("inventoryInput", inventoryInput.serializeNBT(registries)) ;
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction direction) {
        if(direction == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()){
            return myMechanicalBlock;
        }
            return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }
}