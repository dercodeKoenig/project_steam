package NPCs.TownHall;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.gui.modules.guiModuleText;
import ARLib.gui.modules.guiModuleTextInput;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import NPCs.NPCBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

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
    guiModuleTextInput townNameInput;

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

        guiHandler.getModules().add(new guiModuleText(5001, "Name:", guiHandler, 10, 60, 0xff000000, false));
        townNameInput = new guiModuleTextInput(5000, guiHandler, 50, 60, 100, 10){
            @Override
            public void server_readNetworkData(CompoundTag tag){
                super.server_readNetworkData(tag);
                NPCBase.updateAllTownHalls(); // update if the name changes
                TownHallNames.setName(level,getBlockPos(),text);
            }
        };

        guiHandler.getModules().add(townNameInput);
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
        String name = TownHallNames.getName(level,getBlockPos());
        if(name != null)
            townNameInput.text =name;
        else{
            System.out.println("for some reason, the town at "+getBlockPos()+" does not have a name entry");
        }
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
