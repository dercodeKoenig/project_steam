package ARLib.blocks;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.ArrayList;
import java.util.List;

import static ARLib.ARLibRegistry.BLOCK_COIL_COPPER;
import static ARLib.ARLibRegistry.BLOCK_STRUCTURE;

public class BlockCoilCopperBlock extends Block {
    public BlockCoilCopperBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this,1));
        return drops;
    }
}
