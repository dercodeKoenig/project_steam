package ProjectSteam.Blocks.Mechanics.HandGenerator;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import static ProjectSteam.Registry.ENTITY_HAND_GENERATOR;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityHandGenerator extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    public double myForce = 0;

    public static double MAX_FORCE = 100;
    public static double MAX_SPEED = 20;

    int ticksRemainingForForce = 0;

    double myFriction = 2;
    double myInertia = 5;
    double maxStress = 10000;

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
            double actualForce = myForce * Math.max(0, (1 - Math.abs(internalVelocity) / MAX_SPEED));
            return actualForce;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
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

    @Override
    public void setRemoved() {
        super.setRemoved();
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

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (myState.getBlock() instanceof BlockHandGenerator) {
            if (side == myState.getValue(BlockHandGenerator.FACING))
                return myMechanicalBlock;
        }
        return null;
    }

    public EntityHandGenerator(BlockPos pos, BlockState blockState) {
        super(ENTITY_HAND_GENERATOR.get(), pos, blockState);
    }


    public boolean onPlayerClicked(boolean isShift) {
        if (!isShift) {
            if (ticksRemainingForForce < 5) {
                ticksRemainingForForce += 5;
                return true;
            }
        } else {
            if (ticksRemainingForForce > -5) {
                ticksRemainingForForce -= 5;
                return true;
            }
        }
        return false;
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();

        if (ticksRemainingForForce > 0) {
            ticksRemainingForForce--;
            myForce = MAX_FORCE;
        } else if (ticksRemainingForForce < 0) {
            ticksRemainingForForce++;
            myForce = -MAX_FORCE;
        } else {
            myForce = 0;
        }

        if(level.random.nextFloat() < 0.01*Math.abs(myMechanicalBlock.internalVelocity)) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.002f * (float) Math.abs(myMechanicalBlock.internalVelocity), 1.0f);  //
        }
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityHandGenerator) t).tick();
    }
}
