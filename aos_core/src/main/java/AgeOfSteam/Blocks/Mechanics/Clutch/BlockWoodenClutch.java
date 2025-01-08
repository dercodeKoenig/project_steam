package AgeOfSteam.Blocks.Mechanics.Clutch;

import AgeOfSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static AgeOfSteam.Registry.ENTITY_CLUTCH;

public class BlockWoodenClutch extends BlockClutchBase{

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_CLUTCH.get().create(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Max Stress: "+ Config.INSTANCE.wooden_clutch_max_stress));
        tooltipComponents.add(Component.literal("Max Force: "+ Config.INSTANCE.wooden_clutch_max_force));
        tooltipComponents.add(Component.literal("Friction: "+Config.INSTANCE.wooden_clutch_friction_per_side *2));
        tooltipComponents.add(Component.literal("Inertia: "+ Config.INSTANCE.wooden_clutch_inertia_per_side *2));
    }
}
