package NPCs.Items;

import NPCs.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class ItemFoodOrder extends Item {
    public ItemFoodOrder() {
        super(new Properties());
    }

    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            CompoundTag info = new CompoundTag();
            info.putInt("x", context.getClickedPos().getX());
            info.putInt("y", context.getClickedPos().getY());
            info.putInt("z", context.getClickedPos().getZ());
            info.putInt("face", context.getClickedFace().ordinal());
            Utils.setStackTag(context.getItemInHand(), info);
            if (context.getPlayer() != null)
                context.getPlayer().sendSystemMessage(Component.literal("position set to " + context.getClickedPos() + ":" + context.getClickedFace()));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag itemTag = Utils.getStackTagOrEmpty(stack);
        if (itemTag.contains("x") && itemTag.contains("y") && itemTag.contains("z") && itemTag.contains("face")) {
            BlockPos targetPos = new BlockPos(itemTag.getInt("x"), itemTag.getInt("y"), itemTag.getInt("z"));
            Direction face = Direction.values()[itemTag.getInt("face")];
            tooltipComponents.add(Component.literal(targetPos.toShortString()));
            tooltipComponents.add(Component.literal(face.toString()));
        }
    }
}
