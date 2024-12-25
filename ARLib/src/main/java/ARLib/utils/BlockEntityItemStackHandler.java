package ARLib.utils;

import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityItemStackHandler extends net.neoforged.neoforge.items.ItemStackHandler {

    BlockEntity e;
    public BlockEntityItemStackHandler(int size, BlockEntity parentBlockEntity) {
        super(size);
        this.e = parentBlockEntity;
    }
    @Override
    protected void onContentsChanged(int slot){
        e.setChanged();
    }
}
