package ProjectSteam.Blocks.Mechanics.CrankShaft;

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

import static ProjectSteam.Registry.ENTITY_BIG_WOODEN_CRANKSHAFT;

public class BlockBigWoodenCrankShaft extends BlockCrankShaftBase {
    public BlockBigWoodenCrankShaft() {
        super(Properties.of().noOcclusion().strength(1.0f));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_BIG_WOODEN_CRANKSHAFT.get().create(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Max Stress: "+ Config.INSTANCE.wooden_crankshaft_big_max_stress));
        tooltipComponents.add(Component.literal("Friction: "+Config.INSTANCE.wooden_crankshaft_big_friction));
        tooltipComponents.add(Component.literal("Inertia: "+ Config.INSTANCE.wooden_crankshaft_big_inertia));
    }
}