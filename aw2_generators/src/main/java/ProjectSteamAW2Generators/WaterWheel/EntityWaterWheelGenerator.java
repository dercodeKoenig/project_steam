package ProjectSteamAW2Generators.WaterWheel;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

import static ProjectSteamAW2Generators.Registry.ENTITY_WATERWHEEL_GENERATOR;


public class EntityWaterWheelGenerator extends BlockEntity implements INetworkTagReceiver, IMechanicalBlockProvider {

    public static double defaultFriction = 1;
    public static double maxForceMultiplier = 100;
    public static double k = 100;

    double myFriction = 1;
    double myInertia = 10;
    double maxStress = 600;
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

    public EntityWaterWheelGenerator(BlockPos pos, BlockState blockState) {
        super(ENTITY_WATERWHEEL_GENERATOR.get(), pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
    }

    boolean isBlockStateValid(BlockState s){
        return s.getBlock() == Blocks.AIR || s.getFluidState().getType() == Fluids.WATER ||s.getFluidState().getType() == Fluids.FLOWING_WATER;
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();

        boolean canRun = true;

        double outputForce = 0;
        Direction myFacing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos wheelCenterPos = getBlockPos().relative(myFacing.getOpposite());
        BlockPos lowerCenterPos =wheelCenterPos.below();
        BlockPos lowerCorner1 = lowerCenterPos.relative(myFacing.getClockWise());
        BlockPos lowerCorner2 = lowerCenterPos.relative(myFacing.getCounterClockWise());

        if(!isBlockStateValid(level.getBlockState(lowerCenterPos)))canRun = false;
        if(!isBlockStateValid(level.getBlockState(lowerCenterPos.above())))canRun = false;
        if(!isBlockStateValid(level.getBlockState(lowerCenterPos.above().above())))canRun = false;

        BlockState bs1 = level.getBlockState(lowerCorner1);
        if(!isBlockStateValid(bs1))canRun = false;
        int waterLevel1 = 0;
        if(bs1.getFluidState().getType() == Fluids.WATER) {
            waterLevel1 = 20;
        }
        if(bs1.getFluidState().getType() == Fluids.FLOWING_WATER) {
            waterLevel1= bs1.getFluidState().getAmount();
        }
        BlockState bs2 = level.getBlockState(lowerCorner2);
        if(!isBlockStateValid(bs2))canRun = false;
        int waterLevel2 = 0;
        if(bs2.getFluidState().getType() == Fluids.WATER) {
            waterLevel2 = 20;
        }
        if(bs2.getFluidState().getType() == Fluids.FLOWING_WATER) {
            waterLevel2= bs2.getFluidState().getAmount();
        }
        if(waterLevel1 > waterLevel2)
            outputForce+=1;
        else if(waterLevel1 < waterLevel2)
            outputForce-=1;


        BlockPos center1 = lowerCorner1.above();
        BlockState bs3 = level.getBlockState(center1);
        if(!isBlockStateValid(bs3))canRun = false;
        if(bs3.getFluidState().getType() == Fluids.WATER || bs3.getFluidState().getType() == Fluids.FLOWING_WATER) {
            outputForce+=2;
        }


        BlockPos center2 = lowerCorner2.above();
        BlockState bs4 = level.getBlockState(center2);
        if(!isBlockStateValid(bs4))canRun = false;
        if(bs4.getFluidState().getType() == Fluids.WATER || bs4.getFluidState().getType() == Fluids.FLOWING_WATER) {
            outputForce-=2;
        }

        if(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            outputForce = -outputForce;


        if(!isBlockStateValid(level.getBlockState(center1.above())))canRun = false;
        if(!isBlockStateValid(level.getBlockState(center2.above())))canRun = false;

        if(!canRun)myFriction = 3000;
        else myFriction = defaultFriction;

        myForce =  outputForce* maxForceMultiplier - k*Math.abs(outputForce) * myMechanicalBlock.internalVelocity;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityWaterWheelGenerator) t).tick();
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        myMechanicalBlock.mechanicalReadServer(compoundTag);
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        myMechanicalBlock.mechanicalReadClient(compoundTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        myMechanicalBlock.mechanicalLoadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        myMechanicalBlock.mechanicalSaveAdditional(tag, registries);
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