package BetterPipes;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

public class Registry {
    public static final net.neoforged.neoforge.registries.DeferredRegister<Block> BLOCKS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK, "betterpipes");
    public static final net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "betterpipes");
    public static final net.neoforged.neoforge.registries.DeferredRegister<Item> ITEMS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.ITEM, "betterpipes");

    public static void registerBlockItem(String name, DeferredHolder<Block,Block> b){
        ITEMS.register(name,() -> new BlockItem(b.get(), new Item.Properties()));
    }

    public static final DeferredHolder<Block, Block> PIPE = BLOCKS.register(
            "pipe",
            () -> new BlockPipe(BlockBehaviour.Properties.of().noOcclusion().strength(1.0f).instabreak())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntityPipe>> ENTITY_PIPE = BLOCK_ENTITIES.register(
            "entity_pipe",
            () -> BlockEntityType.Builder.of(EntityPipe::new, PIPE.get()).build(null)
    );

    public static void register(IEventBus modBus) {
        registerBlockItem("pipe", PIPE);

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

}
