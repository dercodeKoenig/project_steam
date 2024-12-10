package ProjectSteam.api;

import ARLib.network.INetworkTagReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class MechanicalPartBlockEntityBaseExample extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {
    public MechanicalPartBlockEntityBaseExample(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public double myMass = 1;
    public double myFriction = 0.1;
    public double maxStress = 500;

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0,this) {
        @Override
        public double getMaxStress() {
            return maxStress;
        }
        @Override
        public double getMass(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return myMass;
        }

        @Override
        public double getTorqueResistance(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return myFriction;
        }

        @Override
        public double getTorqueProduced(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace, @org.jetbrains.annotations.Nullable BlockState myState) {
            return 1;
        }

        @Override
        public void onPropagatedTickEnd() {

        }
    };
@Override
    public BlockEntity getBlockEntity(){return this;}

    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
    }


    public void tick() {
        myMechanicalBlock.mechanicalTick();
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
}

