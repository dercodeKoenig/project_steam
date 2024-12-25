package ARLib.blockentities;


import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.network.INetworkTagReceiver;
import ARLib.utils.BlockEntityItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

import static ARLib.ARLibRegistry.ENTITY_ITEM_INPUT_BLOCK;
import static ARLib.ARLibRegistry.ENTITY_ITEM_OUTPUT_BLOCK;

public class EntityItemOutputBlock extends EntityItemInputBlock implements IItemHandler, INetworkTagReceiver {

    public EntityItemOutputBlock(BlockPos pos, BlockState blockState) {
        super(ENTITY_ITEM_OUTPUT_BLOCK.get(),pos, blockState);
    }
}
