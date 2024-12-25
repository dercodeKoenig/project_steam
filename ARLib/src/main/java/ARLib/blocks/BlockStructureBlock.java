package ARLib.blocks;

import ARLib.multiblockCore.BlockMultiblockPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.ArrayList;
import java.util.List;

import static ARLib.ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK;
import static ARLib.ARLibRegistry.BLOCK_STRUCTURE;

public class BlockStructureBlock extends Block {
    public BlockStructureBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this,1));
        return drops;
    }
}
