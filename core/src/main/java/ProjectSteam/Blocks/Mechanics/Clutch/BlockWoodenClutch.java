package ProjectSteam.Blocks.Mechanics.Clutch;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static ProjectSteam.Registry.ENTITY_CLUTCH;

public class BlockWoodenClutch extends BlockClutchBase{

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_CLUTCH.get().create(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Max Stress: "+ Config.INSTANCE.WOODEN_CLUTCH_MAX_STRESS));
        tooltipComponents.add(Component.literal("Friction: "+Config.INSTANCE.WOODEN_CLUTCH_FRICTION_PER_SIDE *2));
        tooltipComponents.add(Component.literal("Inertia: "+ Config.INSTANCE.WOODEN_CLUTCH_INERTIA_PER_SIDE*2));
    }
}