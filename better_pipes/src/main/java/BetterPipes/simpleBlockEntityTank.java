package BetterPipes;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class simpleBlockEntityTank extends FluidTank {
    BlockEntity parent;
    public simpleBlockEntityTank(int capacity, BlockEntity parent) {
        super(capacity);
        this.parent = parent;

    }
    @Override
    protected void onContentsChanged() {
        this.parent.setChanged();
    }

}
