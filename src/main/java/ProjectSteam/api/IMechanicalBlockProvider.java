package ProjectSteam.api;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IMechanicalBlockProvider{
    public AbstractMechanicalBlock getMechanicalBlock(Direction side);
    public BlockEntity getBlockEntity();
}


