package BetterPipes;

import com.sun.jdi.connect.spi.TransportService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.concurrent.atomic.AtomicReference;

import static BetterPipes.EntityPipe.*;

public class PipeConnection implements IFluidHandler {
    Direction myDirection;

    public int lastInputFromInside;
    public int lastInputFromOutside;
    public boolean getsInputFromInside;
    public boolean getsInputFromOutside;

    public int lastOutputToOutside;
    public int lastOutputToInside;
    public boolean outputsToInside;
    public boolean outputsToOutside;

    public int ticksWithFluidInTank = 0;

    simpleBlockEntityTank tank;
    int lastFill;
    public final LazyOptional<IFluidHandler> fluidHandlerOptional = LazyOptional.of(() -> this);


    fluidRenderData renderData;
    EntityPipe parent;

    boolean isEnabled(BlockState parent) {
        return parent.getValue(BlockPipe.connections.get(myDirection)) == BlockPipe.ConnectionState.EXTRACTION || parent.getValue(BlockPipe.connections.get(myDirection)) == BlockPipe.ConnectionState.CONNECTED;
    }

    IFluidHandler neighborFluidHandler() {
        BlockPos neighborPos = parent.getBlockPos().relative(myDirection);
        BlockEntity be = parent.getLevel().getBlockEntity(neighborPos);
        if(be==null)return null;
        LazyOptional<IFluidHandler> fluidHandlerOptional = be.getCapability(ForgeCapabilities.FLUID_HANDLER,myDirection.getOpposite());
        AtomicReference<IFluidHandler> result = new AtomicReference<>(null);
        fluidHandlerOptional.ifPresent(result::set);
        return result.get();
    }

    public PipeConnection(EntityPipe parent, Direction myDirection) {
        this.myDirection = myDirection;
        tank = new simpleBlockEntityTank(CONNECTION_CAPACITY, parent);
        this.parent = parent;
        renderData = new fluidRenderData();
    }

    boolean last_getsInputFromInside;
    boolean last_getsInputFromOutside;
    boolean last_outputsToInside;
    boolean last_outputsToOutside;
    FluidStack last_tankFluid = FluidStack.EMPTY;



    public void saveAdditional(CompoundTag tag){
        CompoundTag myTag = new CompoundTag();
        myTag.putBoolean("getsInputFromInside", getsInputFromInside);
        myTag.putBoolean("getsInputFromOutside", getsInputFromOutside);
        myTag.putBoolean("outputsToInside", outputsToInside);
        myTag.putBoolean("outputsToOutside", outputsToOutside);

        tank.writeToNBT(myTag);
        tag.put(myDirection.getName(), myTag);
    }
    public void loadAdditional(CompoundTag tag){
        CompoundTag myTag = tag.getCompound(myDirection.getName());
        tank.readFromNBT(myTag);

        getsInputFromInside = myTag.getBoolean("getsInputFromInside");
        getsInputFromOutside = myTag.getBoolean("getsInputFromOutside");
        outputsToInside = myTag.getBoolean("outputsToInside");
        outputsToOutside = myTag.getBoolean("outputsToOutside");

    }
void sync(){
    // Check if the tank fluid stack has changed
    // this has it's own packet now for efficiency
    // to not always send the large nbt
    if (!FluidStack.areFluidStackTagsEqual(last_tankFluid, tank.getFluid()) || !last_tankFluid.getFluid().isSame(tank.getFluid().getFluid())) {
        if(!tank.getFluid().isEmpty()) {
            Channel.sendToPlayersTrackingBE(new PacketFluidUpdate(parent.getBlockPos(), myDirection.ordinal(), tank.getFluid().getFluid(),System.currentTimeMillis()), parent);
        }
    }
    if(last_tankFluid.getAmount() != tank.getFluidAmount()){
        Channel.sendToPlayersTrackingBE(new PacketFluidAmountUpdate(parent.getBlockPos(), myDirection.ordinal(), tank.getFluidAmount(),System.currentTimeMillis()), parent);
    }
    last_tankFluid = tank.getFluid().copy(); // Update the last known tank fluid



    boolean needsUpdate = false;

    // Check if the input from inside state has changed
    if (last_getsInputFromInside != getsInputFromInside) {
        needsUpdate = true;
        last_getsInputFromInside = getsInputFromInside;
    }

    // Check if the input from outside state has changed
    if (last_getsInputFromOutside != getsInputFromOutside) {
        needsUpdate = true;
        last_getsInputFromOutside = getsInputFromOutside;
    }

    // Check if the output to inside state has changed
    if (last_outputsToInside != outputsToInside) {
        needsUpdate = true;
        last_outputsToInside = outputsToInside;
    }

    // Check if the output to outside state has changed
    if (last_outputsToOutside != outputsToOutside) {
        needsUpdate = true;
        last_outputsToOutside = outputsToOutside;
    }

    if(needsUpdate){
        Channel.sendToPlayersTrackingBE(new PacketFlowUpdate(parent.getBlockPos(), myDirection.ordinal(), getsInputFromOutside,getsInputFromInside, outputsToOutside, outputsToInside,System.currentTimeMillis()), parent);
    }
}
    void update() {
        if (lastInputFromOutside < STATE_UPDATE_TICKS + 1)
            lastInputFromOutside++;
        else if (getsInputFromOutside)
            getsInputFromOutside = false;

        if (lastInputFromInside < STATE_UPDATE_TICKS + 1)
            lastInputFromInside++;
        else if (getsInputFromInside)
            getsInputFromInside = false;

        if (lastOutputToInside < STATE_UPDATE_TICKS + 1)
            lastOutputToInside++;
        else if (outputsToInside)
            outputsToInside = false;

        if (lastOutputToOutside < STATE_UPDATE_TICKS + 1)
            lastOutputToOutside++;
        else if (outputsToOutside)
            outputsToOutside = false;

        if (!tank.isEmpty() && ticksWithFluidInTank < FORCE_OUTPUT_AFTER_TICKS + 1)
            ticksWithFluidInTank++;
        else if (tank.isEmpty()) {
            ticksWithFluidInTank = 0;
        }
    }
    public void sendInitialTankUpdates(ServerPlayer player) {
        if (!last_tankFluid.isEmpty()) {
            Channel.sendToPlayer(new PacketFluidUpdate(parent.getBlockPos(), myDirection.ordinal(), tank.getFluid().getFluid(), System.currentTimeMillis()), player);
            Channel.sendToPlayer(new PacketFluidAmountUpdate(parent.getBlockPos(), myDirection.ordinal(), tank.getFluidAmount(), System.currentTimeMillis()), player);
            Channel.sendToPlayer(new PacketFlowUpdate(parent.getBlockPos(), myDirection.ordinal(), getsInputFromOutside,getsInputFromInside, outputsToOutside, outputsToInside,System.currentTimeMillis()), player);
        }
    }

    long lastFlowUpdate;
    public void setFlow(boolean ii,boolean io, boolean oi,boolean oo, long time){
        if(time > lastFlowUpdate){
            lastFlowUpdate = time;
            getsInputFromOutside = io;
            getsInputFromInside = ii;
            outputsToInside = oi;
            outputsToOutside = oo;
            parent.setRequiresMeshUpdate();
        }
    }

    long lastFluidInTankUpdate;
    public void setFluidInTank(ResourceLocation f, long time){
        if(time > lastFluidInTankUpdate) {
            lastFluidInTankUpdate = time;
            tank.setFluid(new FluidStack(BuiltInRegistries.FLUID.get(f), Math.max(1,tank.getFluidAmount())));

            parent.setRequiresMeshUpdate();
            if(neighborFluidHandler() instanceof PipeConnection p)
                p.parent.setRequiresMeshUpdate();
        }
    }

    long lastFluidAmountUpdate;
    public void setFluidAmountInTank(int amount, long time) {
        if (time > lastFluidAmountUpdate) {
            lastFluidAmountUpdate = time;
            Fluid myFluid = tank.getFluid().getFluid();
            if (myFluid == Fluids.EMPTY && amount > 0) myFluid = Fluids.WATER;
            if(amount <= 0) myFluid = Fluids.EMPTY;
            tank.setFluid(new FluidStack(myFluid, amount));

            parent.setRequiresMeshUpdate();
            if(neighborFluidHandler() instanceof PipeConnection p)
                p.parent.setRequiresMeshUpdate();
        }
    }

    @Override
    public int getTanks() {
        return this.tank.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.tank.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tank.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return this.tank.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return fill(resource, action, false);
    }

    public int fill(FluidStack resource, FluidAction action, boolean wasFromMaster) {
        int filled = this.tank.fill(resource, action);
        if (filled > 0 && action == FluidAction.EXECUTE) {
            if (wasFromMaster) {
                lastInputFromInside = 0;
                getsInputFromInside = true;
            } else {
                lastInputFromOutside = 0;
                getsInputFromOutside = true;
            }
        }
        return filled;
    }


    void drainUpdate(boolean wasFromMaster) {
        if (wasFromMaster) {
            outputsToInside = true;
            lastOutputToInside = 0;
        } else {
            outputsToOutside = true;
            lastOutputToOutside = 0;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return drain(resource, action, false);
    }

    public FluidStack drain(FluidStack resource, FluidAction action, boolean wasFromMaster) {
        FluidStack drained = this.tank.drain(resource, action);
        if (!drained.isEmpty() && action == FluidAction.EXECUTE) {
            drainUpdate(wasFromMaster);
        }
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return drain(maxDrain, action, false);
    }

    public FluidStack drain(int maxDrain, FluidAction action, boolean wasFromMaster) {
        FluidStack drained = this.tank.drain(maxDrain, action);
        if (!drained.isEmpty() && action == FluidAction.EXECUTE) {
            drainUpdate(wasFromMaster);
        }
        return drained;
    }
}