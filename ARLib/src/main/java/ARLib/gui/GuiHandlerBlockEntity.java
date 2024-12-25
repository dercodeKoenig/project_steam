package ARLib.gui;

import ARLib.gui.modules.GuiModuleBase;
import ARLib.network.PacketBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;


/**
 * How to use a GuiHandler on a BlockEntity
 *
 *
 *  - create a GuiHandlerBlockEntity instance in you BlockEntity class:
 *                 IGuiHandler guiHandler = new GuiHandlerBlockEntity(this);
 *
 *
 *  - register gui modules:
 *                  guiHandler.registerModule(
 *                      new guiModuleEnergy(0,[your_IEnergyStorage_object],guiHandler,10,10)
 *                  );
 *
 *  - implement INetworkTagReceiver in your BlockEntity class
 *    This IGuiHandler uses PacketBlockEntity to send data to your BlockEntity.
 *    You need to forward this data to the IGuiHandler:
 *
 *          in  readServer(CompoundTag tag) call guiHandler.readServer(CompoundTag tag)
 *          in  readClient(CompoundTag tag) call guiHandler.readClient(CompoundTag tag)
 *
 *  - register your BlockEntity to have a tick() method
 *    In IGuiHandler.serverTick(...), the server scans for changes in the gui data if one or more clients watch the gui.
 *    You need to call guiHandler.serverTick([your_gui_handler_instance]) on server side every tick to allow data sync.
 *    If no clients watch the gui, serverTick will instantly return to keep the code efficient and not waste time.
 *
 *    example to use for your Block class:
 *     @Override
 *     public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
 *         return EntityLathe::tick;
 *     }
 *    example to use for your BlockEntity class:
 *     public static <x extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, x t) {
 *         if(!level.isClientSide)
 *             ((EntityLathe)t).guiHandler.serverTick();
 *     }
 *
 *
 *  - open your gui from anywhere using [your_gui_handler_instance].openGui(), for example on block click.
 *    example:
 *     @Override
 *     public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
 *         BlockEntity e = world.getBlockEntity(pos);
 *         if (e instanceof EntityLathe ee) {
 *             if (world.isClientSide) {
 *                 ee.guiHandler.openGui();
 *             }
 *         }
 *         return InteractionResult.SUCCESS;
 *     }
 */
public class GuiHandlerBlockEntity implements IGuiHandler {

    Map<UUID, Integer> playersTrackingGui;
    List<GuiModuleBase> modules;
    int last_ping = 0;
    BlockEntity parentBE;

    public GuiHandlerBlockEntity(BlockEntity parentBlockEntity) {
        this.playersTrackingGui = new HashMap<>();
        modules = new ArrayList<>();
        this.parentBE = parentBlockEntity;
    }

    public void registerModule(GuiModuleBase guiModule) {
        modules.add(guiModule);
    }

    @Override
    public List<GuiModuleBase> getModules() {
        return modules;
    }

    @Override
    public Map<UUID, Integer> getPlayersTrackingGui(){
        return playersTrackingGui;
    }

    @Override
    public CustomPacketPayload getNetworkPacketForTag_client(CompoundTag tag) {
        return PacketBlockEntity.getBlockEntityPacket(parentBE,tag);
    }
    @Override
    public CustomPacketPayload getNetworkPacketForTag_server(CompoundTag tag) {
        return PacketBlockEntity.getBlockEntityPacket(parentBE,tag);
    }

    public void onGuiClientTick() {
        last_ping += 1;
        if (last_ping > 20) {
            last_ping = 0;
            sendPing();
        }
    }

}
