package ProjectSteam.Items.Hammer;

import ProjectSteam.Blocks.Mechanics.CrankShaft.EntityCrankShaftBase;
import ProjectSteam.Blocks.Mechanics.TJunction.BlockTJunctionBase;
import ProjectSteam.Blocks.Mechanics.TJunction.EntityTJunctionBase;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

public class ItemHammer extends Item {
    public ItemHammer() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        BlockEntity tile =context.getLevel().getBlockEntity(context.getClickedPos());

        if(tile instanceof EntityTJunctionBase i){
            BlockState s  = i.getBlockState();
            if(!context.getLevel().isClientSide()) {
                s = s.setValue(BlockTJunctionBase.INVERTED, !s.getValue(BlockTJunctionBase.INVERTED));
                context.getLevel().setBlock(context.getClickedPos(), s, 3);
            }
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }

        if(tile instanceof EntityCrankShaftBase i) {
            if(!context.getLevel().isClientSide()) {
                i.incRotationOffset();
                i.myMechanicalBlock.propagateResetRotation(0, null, new HashSet<>());
            }
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }

        return InteractionResult.PASS;
    }

}
