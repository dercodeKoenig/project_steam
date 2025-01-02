package Farms.CropFarm;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.ModularScreen;
import ARLib.gui.modules.guiModuleDefaultButton;
import ARLib.gui.modules.guiModuleImage;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

import static Farms.Registry.ENTITY_CROP_FARM;

public class EntityCropFarm extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    GuiHandlerBlockEntity guiHandlerMain;
    GuiHandlerBlockEntity guiHandlerBounds;

    int w = 5;
    int h = 5;
    int controllerOffset = 0;
    int maxSize = 16;

    BlockPos pmin;
    BlockPos pmax;

    public EntityCropFarm(BlockPos pos, BlockState blockState) {
        super(ENTITY_CROP_FARM.get(), pos, blockState);
        guiHandlerMain = new GuiHandlerBlockEntity(this);

        pmax = new BlockPos(0, 0, 0);
        pmin = new BlockPos(0, 0, 0);

        guiModuleDefaultButton openBoundsGuiButton = new guiModuleDefaultButton(0, "bounds", guiHandlerMain, 10, 80, 40, 10) {
            @Override
            public void onButtonClicked() {
                guiHandlerBounds.openGui(200, 200, true);
            }
        };
        guiHandlerMain.getModules().add(openBoundsGuiButton);

        guiHandlerBounds = new GuiHandlerBlockEntity(this) {
            @Override
            public void onGuiClose() {
                EntityCropFarm.this.openMainGui();
            }
        };
        updateGuiModules();
    }

    @Override
    public void onLoad() {
        if (level.isClientSide) {
            CompoundTag t = new CompoundTag();
            t.putUUID("client_onload", Minecraft.getInstance().player.getUUID());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, t));
        }
    }

    public void updateBoundsBp() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos p1 = getBlockPos().relative(facing.getOpposite());
        p1 = p1.relative(facing.getClockWise(), controllerOffset);
        BlockPos p2 = p1.relative(facing.getCounterClockWise(), w - 1).relative(facing.getOpposite(), h - 1);

        pmin = new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
        pmax = new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));
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
                guiModuleImage controllerPos = new guiModuleImage(guiHandlerBounds, x * pxPerBlock + baseOffsetX, y * pxPerBlock + baseOffsetY, pxPerBlock, pxPerBlock, red, 1, 1);
                guiHandlerBounds.getModules().add(controllerPos);
            }
        }

        int controllerX = baseOffsetX + controllerOffset * pxPerBlock;
        int controllerY = baseOffsetY + h * pxPerBlock;
        guiModuleImage controllerPos = new guiModuleImage(guiHandlerBounds, controllerX, controllerY, pxPerBlock, pxPerBlock, blue, 1, 1);
        guiHandlerBounds.getModules().add(controllerPos);


        guiModuleDefaultButton hinc = new guiModuleDefaultButton(101, "h+", guiHandlerBounds, 10, 10, 20, 15);
        guiModuleDefaultButton hdec = new guiModuleDefaultButton(102, "h-", guiHandlerBounds, 40, 10, 20, 15);
        guiHandlerBounds.getModules().add(hinc);
        guiHandlerBounds.getModules().add(hdec);

        guiModuleDefaultButton winc = new guiModuleDefaultButton(103, "w+", guiHandlerBounds, 70, 10, 20, 15);
        guiModuleDefaultButton wdec = new guiModuleDefaultButton(104, "w-", guiHandlerBounds, 100, 10, 20, 15);
        guiHandlerBounds.getModules().add(winc);
        guiHandlerBounds.getModules().add(wdec);

        guiModuleDefaultButton oinc = new guiModuleDefaultButton(105, "o+", guiHandlerBounds, 130, 10, 20, 15);
        guiModuleDefaultButton odec = new guiModuleDefaultButton(106, "o-", guiHandlerBounds, 160, 10, 20, 15);
        guiHandlerBounds.getModules().add(oinc);
        guiHandlerBounds.getModules().add(odec);


        if (guiHandlerBounds.screen instanceof ModularScreen ms) {
            ms.calculateGuiOffsetAndNotifyModules();
        }
    }

    public void openMainGui() {
        if (level.isClientSide) {
            guiHandlerMain.openGui(180, 200, true);
        }
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction direction) {
        return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }


    public CompoundTag getUpdateTag() {
        CompoundTag t = new CompoundTag();
        t.putInt("controllerOffset", controllerOffset);
        t.putInt("maxSize", maxSize);
        t.putInt("w", w);
        t.putInt("h", h);
        return t;
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        if (compoundTag.contains("client_onload")) {
            UUID from = compoundTag.getUUID("client_onload");
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            if (p != null) {
                PacketDistributor.sendToPlayer(p, PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
            }
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
                //inc controllerOffset
                if (controllerOffset < maxSize) controllerOffset++;
            }
            if (button == 106) {
                //dec controllerOffset
                if (controllerOffset > 0) controllerOffset--;
            }
            controllerOffset = Math.min(w-1, controllerOffset);

            updateBoundsBp();
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(getBlockPos()), PacketBlockEntity.getBlockEntityPacket(this, getUpdateTag()));
        }
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        if (compoundTag.contains("controllerOffset")) {
            controllerOffset = compoundTag.getInt("controllerOffset");
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
        updateGuiModules();
        updateBoundsBp();
    }
}
