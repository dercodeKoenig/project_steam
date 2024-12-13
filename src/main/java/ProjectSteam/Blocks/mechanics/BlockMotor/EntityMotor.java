package ProjectSteam.Blocks.mechanics.BlockMotor;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.guiModuleEnergy;
import ARLib.network.INetworkTagReceiver;
import ARLib.utils.BlockEntityBattery;
import ProjectSteam.Static;
import ProjectSteam.core.AbstractMechanicalBlock;
import ProjectSteam.core.IMechanicalBlockProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.energy.IEnergyStorage;

import static ProjectSteam.Registry.ENTITY_MOTOR;


public class EntityMotor extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver, IEnergyStorage {

    public double MOTOR_BASE_FRICTION = 5;
    public double K = 10;
    public double MOTOR_EFFICIENCY = 0.95;

    /**
     *
     * I use F = Fmax - k * V to get the current force
     * Fmax = k * Vmax
     * I use P = Fmax * Vmax / 4 because:
     * when using Pin = Pout:
     * Pin = F*V
     * Pin = (Fmax - kV) * V
     * This will have its maximum at V = Vmax/2 with F*V = P/4
     * So... I spent a lot of time figuring out why P = Fmax * Vmax has this horrible efficiency of 25%
     * At the end, I came to conclusion that I will just multiply Fmax by 4 to scale it to 100% max efficiency
     *
     *
     * @param p power
     * @param k motor torque to speed constant
     * @return Fmax
     */
    public static double Fmax_from_p_and_k(double p, double k){
        return Math.sqrt(4*p*k);
    }
    int rfPerTick = 10;

    double currentForceProduced;
    double currentResistance;
    int directionMultiplier = 1;


    VertexBuffer vertexBuffer;
    MeshData mesh;

    public double myMass = 10;

    int lastLight = 0;

    IGuiHandler guiHandler;
    IEnergyStorage energyStorage;


    public EntityMotor(BlockPos pos, BlockState blockState) {
        super(ENTITY_MOTOR.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }
        rfPerTick = 10;


        energyStorage = new BlockEntityBattery(this, 1000);

        guiHandler = new GuiHandlerBlockEntity(this);
        guiModuleEnergy e1 = new guiModuleEnergy(0,this,guiHandler,10,10);
        guiHandler.registerModule(e1);
    }

    public void openGui(){
        if(level.isClientSide)
            guiHandler.openGui(100,100);
    }

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return Double.MAX_VALUE;
        }

        @Override
        public double getMass(Direction face) {
            return myMass;
        }

        @Override
        public double getTorqueResistance(Direction face) {
            return currentResistance;
        }

        @Override
        public double getTorqueProduced(Direction face) {
            return currentForceProduced;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
            return 1;
        }

        @Override
        public void onPropagatedTickEnd() {

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
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer.close();
            });

        }
        super.setRemoved();
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();

        if(!level.isClientSide) {
            IGuiHandler.serverTick(guiHandler);
            K = 3;
            rfPerTick = 50;

            if (level.hasNeighborSignal(getBlockPos())) {
                double facingMultiplier = getBlockState().getValue(BlockMotor.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
                int maxConsumedEnergy = Math.min(getEnergyStored(), rfPerTick);
                double workingForce = facingMultiplier * directionMultiplier * Fmax_from_p_and_k(maxConsumedEnergy, K) - K * myMechanicalBlock.internalVelocity;
                currentForceProduced = workingForce;
                currentResistance = MOTOR_BASE_FRICTION;
                extractEnergy(maxConsumedEnergy, false);

            } else {
                currentForceProduced = 0;
                double workingResistance = Math.abs(-K * myMechanicalBlock.internalVelocity);
                int energyProduced = (int) (Math.abs(myMechanicalBlock.internalVelocity) * workingResistance);
                int receivedEnergy = receiveEnergy(energyProduced, false);
                currentResistance = MOTOR_BASE_FRICTION;
                if (energyProduced > 0) {
                    currentResistance += workingResistance * receivedEnergy / energyProduced;
                }
            }
        }
    }


    @Override
    public void readClient(CompoundTag tag) {
        //System.out.println("readClient:"+tag);
        myMechanicalBlock.mechanicalReadClient(tag);
        guiHandler.readClient(tag);
    }

    @Override
    public void readServer(CompoundTag tag) {
        //System.out.println("readServer:"+tag);
        myMechanicalBlock.mechanicalReadServer(tag);
        guiHandler.readServer(tag);
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
        if (myState.getBlock() instanceof BlockMotor) {
            if (side == myState.getValue(BlockMotor.FACING))
                return myMechanicalBlock;
        }
        return null;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityMotor) t).tick();
    }

    @Override
    public int receiveEnergy(int i, boolean b) {
        return energyStorage.receiveEnergy(i,b);
    }

    @Override
    public int extractEnergy(int i, boolean b) {
        return energyStorage.extractEnergy(i,b);
    }

    @Override
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return energyStorage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return energyStorage.canReceive();
    }
}