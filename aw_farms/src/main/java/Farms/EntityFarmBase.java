package Farms;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.ModularScreen;
import ARLib.gui.modules.guiModuleButton;
import ARLib.gui.modules.guiModuleDefaultButton;
import ARLib.gui.modules.guiModuleImage;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.*;

public abstract class EntityFarmBase extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    public GuiHandlerBlockEntity guiHandlerMain;
    public GuiHandlerBlockEntity guiHandlerBounds;

    public int w = 5;
    public int h = 5;
    public int controllerOffsetW = 0;
    public int controllerOffsetH = 0;
    public int maxSize = 16;

    public Set<Vector2i> blackList = new HashSet<>();
    public Set<BlockPos> blackListAsBlockPos = new HashSet<>();
    public Set<BlockPos> allowedBlocks = new HashSet<>();
    public List<BlockPos> allowedBlocksList = new ArrayList<>();

    public int renderInfoTimer = 0;

    public boolean allowMechanicalPower = true;

    public BlockPos pmin;
    public BlockPos pmax;

    public IEnergyStorage battery = new EnergyStorage(10000) {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int r = super.receiveEnergy(toReceive, simulate);
            if (!simulate) setChanged();
            return r;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int r = super.extractEnergy(toExtract, simulate);
            if (!simulate) setChanged();
            return r;
        }
    };

    public double currentResistance;
    public double k = 10;
    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return 999999;
        }

        @Override
        public double getInertia(Direction direction) {
            return 1;
        }

        @Override
        public double getTorqueResistance(Direction direction) {
            return currentResistance;
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

    public EntityFarmBase(BlockEntityType type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        guiHandlerMain = new GuiHandlerBlockEntity(this);

        pmax = new BlockPos(0, 0, 0);
        pmin = new BlockPos(0, 0, 0);

        guiModuleDefaultButton openBoundsGuiButton = new guiModuleDefaultButton(0, "bounds", guiHandlerMain, 10, 10, 40, 15) {
            @Override
            public void onButtonClicked() {
                guiHandlerBounds.openGui(200, 200, true);
            }
        };
        guiHandlerMain.getModules().add(openBoundsGuiButton);

        guiHandlerBounds = new GuiHandlerBlockEntity(this) {
            @Override
            public void onGuiClose() {
                EntityFarmBase.this.openMainGui();
            }
        };
    }

    @Override
    public void onLoad() {
        if (level.isClientSide) {
            CompoundTag t = new CompoundTag();
            t.putUUID("client_onload", Minecraft.getInstance().player.getUUID());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, t));
        } else {
            updateBoundsBp();
        }
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();
        if (!level.isClientSide) {
            guiHandlerMain.serverTick();
            double maxWorkingResistance = Math.abs(this.k * this.myMechanicalBlock.internalVelocity);
            int energyMaxProduced = (int) (Math.abs(this.myMechanicalBlock.internalVelocity) * maxWorkingResistance);
            int freeEnergyCapacity = battery.getMaxEnergyStored() - battery.getEnergyStored();
            int energyProduced = Math.min(freeEnergyCapacity, energyMaxProduced);
            battery.receiveEnergy(energyProduced, false);
            this.currentResistance = 0;
            if (energyProduced > 0) {
                double actualResistanceProduced = maxWorkingResistance * (double) energyProduced / (double) energyMaxProduced;
                this.currentResistance += actualResistanceProduced;
            }
            //System.out.println(energyProduced+":"+currentResistance);
        }
        if (level.isClientSide) {
            if (renderInfoTimer > 0) {
                renderInfoTimer--;
            }
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityFarmBase) t).tick();
    }

    public void updateBoundsBp() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos p1 = getBlockPos().relative(facing,controllerOffsetH-1);
        p1 = p1.relative(facing.getClockWise(), controllerOffsetW);
        BlockPos p2 = p1.relative(facing.getCounterClockWise(), w - 1).relative(facing.getOpposite(), h - 1);

        pmin = new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
        pmax = new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));


        // remove blacklist entries outside bounds
        boolean sth = true;
        while (sth) {
            sth = false;
            for (Vector2i i : blackList) {
                if (i.x >= w || i.y >= h) {
                    blackList.remove(i);
                    sth = true;
                    break;
                }
            }
        }

        // compute blacklist blockpos to render
        blackListAsBlockPos.clear();
        for (Vector2i i : blackList) {
            BlockPos blocked = p1.relative(facing.getCounterClockWise(), i.x).relative(facing.getOpposite(), i.y);
            blackListAsBlockPos.add(blocked);
        }

        // compute allowed blockpos
        allowedBlocks.clear();
        for (int y = pmin.getY(); y <= pmax.getY(); y++) {
            for (int z = pmin.getZ(); z <= pmax.getZ(); z++) {
                for (int x = pmin.getX(); x <= pmax.getX(); x++) {
                    BlockPos target = new BlockPos(x,y,z);
                    if (!blackListAsBlockPos.contains(target)) {
                        allowedBlocks.add(target);
                    }
                }
            }
        }
        allowedBlocksList = allowedBlocks.stream().toList();
    }

    public void updateGuiModules() {
        guiHandlerBounds.getModules().clear();

        ResourceLocation red = ResourceLocation.fromNamespaceAndPath("aw_farms", "textures/gui/red.png");
        ResourceLocation blue = ResourceLocation.fromNamespaceAndPath("aw_farms", "textures/gui/blue.png");
        ResourceLocation black = ResourceLocation.fromNamespaceAndPath("aw_farms", "textures/gui/black.png");

        int baseOffsetX = 30;
        int baseOffsetY = 30;

        int pxPerBlock = 140 / maxSize;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int _x = x;
                int _y = h - y - 1; // because highest y is directly next to the controller
                if (blackList.contains(new Vector2i(_x, _y))) {
                    guiModuleButton b = new guiModuleButton(-1, "", guiHandlerBounds, x * pxPerBlock + baseOffsetX, y * pxPerBlock + baseOffsetY, pxPerBlock, pxPerBlock, black, 1, 1) {
                        @Override
                        public void onButtonClicked() {
                            CompoundTag i = new CompoundTag();
                            CompoundTag j = new CompoundTag();
                            j.putInt("x", _x);
                            j.putInt("y", _y);
                            i.put("blacklist_remove", j);
                            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(EntityFarmBase.this, i));
                        }
                    };
                    guiHandlerBounds.getModules().add(b);
                } else {
                    guiModuleButton b = new guiModuleButton(-1, "", guiHandlerBounds, x * pxPerBlock + baseOffsetX, y * pxPerBlock + baseOffsetY, pxPerBlock, pxPerBlock, red, 1, 1) {
                        @Override
                        public void onButtonClicked() {
                            CompoundTag i = new CompoundTag();
                            CompoundTag j = new CompoundTag();
                            j.putInt("x", _x);
                            j.putInt("y", _y);
                            i.put("blacklist_add", j);
                            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(EntityFarmBase.this, i));
                        }
                    };
                    guiHandlerBounds.getModules().add(b);
                }
            }
        }

        int controllerX = baseOffsetX + controllerOffsetW * pxPerBlock;
        int controllerY = baseOffsetY + h * pxPerBlock-controllerOffsetH*pxPerBlock;
        guiModuleImage controllerPos = new guiModuleImage(guiHandlerBounds, controllerX, controllerY, pxPerBlock, pxPerBlock, blue, 1, 1);
        guiHandlerBounds.getModules().add(controllerPos);

        guiModuleDefaultButton hinc = new guiModuleDefaultButton(101, "h+", guiHandlerBounds, 5, 10, 15, 15);
        guiModuleDefaultButton hdec = new guiModuleDefaultButton(102, "h-", guiHandlerBounds, 30, 10, 15, 15);
        guiHandlerBounds.getModules().add(hinc);
        guiHandlerBounds.getModules().add(hdec);

        guiModuleDefaultButton winc = new guiModuleDefaultButton(103, "w+", guiHandlerBounds, 55, 10, 15, 15);
        guiModuleDefaultButton wdec = new guiModuleDefaultButton(104, "w-", guiHandlerBounds, 80, 10, 15, 15);
        guiHandlerBounds.getModules().add(winc);
        guiHandlerBounds.getModules().add(wdec);

        guiModuleDefaultButton xinc = new guiModuleDefaultButton(105, "x+", guiHandlerBounds, 105, 10, 15, 15);
        guiModuleDefaultButton xdec = new guiModuleDefaultButton(106, "x-", guiHandlerBounds, 130, 10, 15, 15);
        guiHandlerBounds.getModules().add(xinc);
        guiHandlerBounds.getModules().add(xdec);

        guiModuleDefaultButton yinc = new guiModuleDefaultButton(107, "y+", guiHandlerBounds, 155, 10, 15, 15);
        guiModuleDefaultButton ydec = new guiModuleDefaultButton(108, "y-", guiHandlerBounds, 180, 10, 15, 15);
        guiHandlerBounds.getModules().add(yinc);
        guiHandlerBounds.getModules().add(ydec);


        if (guiHandlerBounds.screen instanceof ModularScreen ms) {
            ms.calculateGuiOffsetAndNotifyModules();
        }
    }

    abstract public void openMainGui();

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction direction) {
        if(allowMechanicalPower)
            return myMechanicalBlock;
        else return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }


    public CompoundTag getUpdateTag() {
        CompoundTag t = new CompoundTag();
        t.putInt("controllerOffsetW", controllerOffsetW);
        t.putInt("controllerOffsetH", controllerOffsetH);
        t.putInt("maxSize", maxSize);
        t.putInt("w", w);
        t.putInt("h", h);
        ListTag blackListTag = new ListTag();
        for (Vector2i i : blackList) {
            CompoundTag o = new CompoundTag();
            o.putInt("x", i.x);
            o.putInt("y", i.y);
            blackListTag.add(o);
        }
        t.put("blacklist", blackListTag);
        return t;
    }

    public void readUpdateTag(CompoundTag compoundTag) {
        if (compoundTag.contains("controllerOffsetW")) {
            controllerOffsetW = compoundTag.getInt("controllerOffsetW");
        }
        if (compoundTag.contains("controllerOffsetH")) {
            controllerOffsetH = compoundTag.getInt("controllerOffsetH");
        }
        if (compoundTag.contains("maxSize")) {
            maxSize = compoundTag.getInt("maxSize");
        }
        if (compoundTag.contains("w")) {
            w = compoundTag.getInt("w");
        }
        if (compoundTag.contains("h")) {
            h = compoundTag.getInt("h");
        }
        if (compoundTag.contains("blacklist")) {
            ListTag blackListTag = compoundTag.getList("blacklist", Tag.TAG_COMPOUND);
            blackList.clear();
            for (int i = 0; i < blackListTag.size(); i++) {
                CompoundTag t = blackListTag.getCompound(i);
                int x = t.getInt("x");
                int y = t.getInt("y");
                blackList.add(new Vector2i(x, y));
            }
        }
        updateGuiModules();
        updateBoundsBp();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        myMechanicalBlock.mechanicalSaveAdditional(tag, registries);
        tag.put("data", getUpdateTag());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        myMechanicalBlock.mechanicalLoadAdditional(tag, registries);
        readUpdateTag(tag.getCompound("data"));
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        guiHandlerMain.readServer(compoundTag);
        myMechanicalBlock.mechanicalReadServer(compoundTag);

        if (compoundTag.contains("client_onload")) {
            UUID from = compoundTag.getUUID("client_onload");
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            if (p != null) {
                PacketDistributor.sendToPlayer(p, PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
            }
        }
        if (compoundTag.contains("blacklist_add")) {
            CompoundTag i = compoundTag.getCompound("blacklist_add");
            Vector2i target = new Vector2i(i.getInt("x"), i.getInt("y"));
            blackList.add(target);
            updateBoundsBp();
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(getBlockPos()), PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
        }
        if (compoundTag.contains("blacklist_remove")) {
            CompoundTag i = compoundTag.getCompound("blacklist_remove");
            Vector2i target = new Vector2i(i.getInt("x"), i.getInt("y"));
            blackList.remove(target);
            updateBoundsBp();
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(getBlockPos()), PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
        }

        if (compoundTag.contains("guiButtonClick")) {
            int button = compoundTag.getInt("guiButtonClick");

            if (button == 101) {
                //inc h
                if (h < maxSize) h++;
            }
            if (button == 102) {
                //dec h
                if (h > 1) h--;
            }
            if (button == 103) {
                //inc w
                if (w < maxSize) w++;
            }
            if (button == 104) {
                //dec w
                if (w > 1) w--;
            }

            if (button == 105) {
                //inc controllerOffsetW
                if (controllerOffsetW < maxSize) controllerOffsetW++;
            }
            if (button == 106) {
                //dec controllerOffsetW
                if (controllerOffsetW > 0) controllerOffsetW--;
            }
            controllerOffsetW = Math.min(w - 1, controllerOffsetW);

            if (button == 107) {
                //inc controllerOffsetH
                if (controllerOffsetH < maxSize) controllerOffsetH++;
            }
            if (button == 108) {
                //dec controllerOffsetH
                if (controllerOffsetH > 0) controllerOffsetH--;
            }
            controllerOffsetH = Math.min(h, controllerOffsetH);

            updateBoundsBp();
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(getBlockPos()), PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
        }
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        myMechanicalBlock.mechanicalReadClient(compoundTag);
        guiHandlerMain.readClient(compoundTag);
        readUpdateTag(compoundTag);
    }
}
