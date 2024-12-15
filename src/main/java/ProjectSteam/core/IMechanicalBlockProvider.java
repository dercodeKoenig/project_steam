package ProjectSteam.Core;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public interface IMechanicalBlockProvider{
    AbstractMechanicalBlock getMechanicalBlock(Direction side);
    BlockEntity getBlockEntity();



    default Map<Direction, AbstractMechanicalBlock> getConnectedParts(IMechanicalBlockProvider mechanicalBlockProvider, @Nullable AbstractMechanicalBlock MechanicalBlock) {

        Map<Direction, AbstractMechanicalBlock> connectedBlocks = new HashMap<>();

        for (Direction i : Direction.values()) {
            AbstractMechanicalBlock mechanicalBlock = mechanicalBlockProvider.getMechanicalBlock(i);
            if(mechanicalBlock != null) {
                BlockEntity otherBE = mechanicalBlock.me.getBlockEntity().getLevel().getBlockEntity(mechanicalBlock.me.getBlockEntity().getBlockPos().relative(i));
                if (otherBE instanceof IMechanicalBlockProvider p) {
                    AbstractMechanicalBlock other = p.getMechanicalBlock(i.getOpposite());
                    if (other instanceof AbstractMechanicalBlock otherMechBlock) {
                        connectedBlocks.put(i, otherMechBlock);
                    }
                }
            }
        }
        return connectedBlocks;
    }
}


