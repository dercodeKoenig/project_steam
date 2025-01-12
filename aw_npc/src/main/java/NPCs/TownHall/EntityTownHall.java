package NPCs.TownHall;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ARLib.utils.DimensionUtils;
import AgeOfSteam.Blocks.Mechanics.Clutch.EntityClutchBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static NPCs.Registry.ENTITY_TOWNHALL;

public class EntityTownHall extends BlockEntity implements INetworkTagReceiver {
    public static HashSet<BlockPos> knownTownHalls = new HashSet<>();

    public ItemStackHandler inventory = new ItemStackHandler(9 * 2) {
        @Override
        public void onContentsChanged(int slot) {
            setChanged();
        }
    };
    GuiHandlerBlockEntity guiHandler;

    public EntityTownHall(BlockPos pos, BlockState blockState) {
        super(ENTITY_TOWNHALL.get(), pos, blockState);
        guiHandler = new GuiHandlerBlockEntity(this);

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 2; y++) {
                guiModuleItemHandlerSlot m = new guiModuleItemHandlerSlot(y * 9 + x, inventory, y * 9 + x, 1, 0, guiHandler, x * 18 + 10, y * 18 + 10);
                guiHandler.getModules().add(m);
            }
        }

        for (guiModulePlayerInventorySlot m : guiModulePlayerInventorySlot.makePlayerHotbarModules(10, 150, 1000, 0, 1, guiHandler)) {
            guiHandler.getModules().add(m);
        }
        for (guiModulePlayerInventorySlot m : guiModulePlayerInventorySlot.makePlayerInventoryModules(10, 80, 1100, 0, 1, guiHandler)) {
            guiHandler.getModules().add(m);
        }
    }

    public Set<String> getOwners() {
        return TownHallOwners.getOwners(level, getBlockPos());
    }

    public void useWithoutItem(Player p) {
        if (!level.isClientSide) {
                if (getOwners().contains(p.getName().getString())) {
                    CompoundTag tag = new CompoundTag();
                    tag.put("openGui", new CompoundTag());
                    PacketDistributor.sendToPlayer((ServerPlayer) p, PacketBlockEntity.getBlockEntityPacket(this, tag));
                }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        knownTownHalls.add(getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        knownTownHalls.remove(getBlockPos());
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityTownHall) t).tick();
    }

    public void tick() {
        if (!level.isClientSide) {
            guiHandler.serverTick();
        }
    }


    @Override
    public void readServer(CompoundTag compoundTag, ServerPlayer p) {
        // verify server side that the player is friend or owner before allow anything to go to the gui
        if (getOwners().contains(p.getName().getString())) {
            guiHandler.readServer(compoundTag);
        }
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        guiHandler.readClient(compoundTag);

        if (compoundTag.contains("openGui")) {
            guiHandler.openGui(180, 200, true);
        }
    }
}
