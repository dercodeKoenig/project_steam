package ARLib.holoProjector;

import ARLib.multiblockCore.EntityMultiblockPlaceholder;
import ProjectSteam.Blocks.Mechanics.Axle.EntityAxleBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static ARLib.ARLibRegistry.ENTITY_STRUCTURE_PREVIEW;


public class BlockStructurePreviewBlock extends Block implements EntityBlock {
    public BlockStructurePreviewBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_STRUCTURE_PREVIEW.get().create(pos,state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockEntity me = world.getBlockEntity(pos);
        if(me instanceof  EntityStructurePreviewBlock esp){
            return esp.getBlockToRender().defaultBlockState().getShape(world,pos, context);
        }
        return Shapes.create(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return EntityStructurePreviewBlock::tick;
    }

}
