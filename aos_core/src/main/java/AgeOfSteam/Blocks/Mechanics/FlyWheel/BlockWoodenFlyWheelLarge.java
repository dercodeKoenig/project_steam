package AgeOfSteam.Blocks.Mechanics.FlyWheel;

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

import static AgeOfSteam.Registry.ENTITY_WOODEN_FLYWHEEL_LARGE;

public class BlockWoodenFlyWheelLarge extends BlockFlyWheelBase {
    public BlockWoodenFlyWheelLarge() {
        super(Properties.of().noOcclusion().strength(1.0f));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_WOODEN_FLYWHEEL_LARGE.get().create(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Max Stress: "+ Config.INSTANCE.wooden_flywheel_large_max_stress));
        tooltipComponents.add(Component.literal("Friction: "+Config.INSTANCE.wooden_flywheel_large_friction));
        tooltipComponents.add(Component.literal("Inertia: "+ Config.INSTANCE.wooden_flywheel_large_inertia));
    }
}