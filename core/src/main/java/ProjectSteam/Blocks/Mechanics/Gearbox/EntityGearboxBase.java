package ProjectSteam.Blocks.Mechanics.Gearbox;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityGearboxBase extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    double myInertia;
    double myFriction;
    double maxStress;

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
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
            if (receivingFace == null) return 1;
            BlockState myState = getBlockState();

            if (myState.getBlock() instanceof BlockGearboxBase) {
                Direction facing = myState.getValue(BlockGearboxBase.FACING);

                if (receivingFace == facing.getOpposite())
                    return (double) -3 / 2;
                if (receivingFace == facing)
                    return (double) -2 / 3;
            }
            return 1;
        }
    };

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
    }


    public void tick() {
        myMechanicalBlock.mechanicalTick();
        if(level.random.nextFloat() < 0.005*Math.abs(myMechanicalBlock.internalVelocity)) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.005f*(float)Math.abs(myMechanicalBlock.internalVelocity), 1.0f);  //
        }
    }


    @Override
    public void readClient(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadClient(tag);
    }

    @Override
    public void readServer(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadServer(tag);
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

    public EntityGearboxBase(BlockEntityType type, BlockPos pos, BlockState blockState) {
        super(type,pos,blockState);
        // because the input/output do not rotate with the same speed, reset only when they all made a full rotation
        // I think the gearbox should have a ratio of 2:3 for both sides to a total of 4:9 or 9:4
        // if we reset after 6 rotations, the high rpm part should have completed 9 rotations and the low rpm part should completed 4 rotations
        myMechanicalBlock.resetRotationAfterX = 360*6;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }


    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (side.getAxis() == myState.getValue(BlockGearboxBase.FACING).getAxis())
            return myMechanicalBlock;
        return null;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityGearboxBase) t).tick();
    }
}