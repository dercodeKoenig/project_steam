package ProjectSteam;

import ProjectSteam.Blocks.Axle.BlockAxle;
import ProjectSteam.Blocks.Axle.EntityAxle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

public class Registry {
    public static final net.neoforged.neoforge.registries.DeferredRegister<Block> BLOCKS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK, "projectsteam");
    public static final net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "projectsteam");
    public static final net.neoforged.neoforge.registries.DeferredRegister<Item> ITEMS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.ITEM, "projectsteam");

    public static void registerBlockItem(String name, DeferredHolder<Block,Block> b){
        ITEMS.register(name,() -> new BlockItem(b.get(), new Item.Properties()));
    }

    public static final DeferredHolder<Block, Block> AXLE = BLOCKS.register(
            "axle",
            () -> new BlockAxle(BlockBehaviour.Properties.of().noOcclusion().strength(1.0f))
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntityAxle>> ENTITY_AXLE = BLOCK_ENTITIES.register(
            "entity_axle",
            () -> BlockEntityType.Builder.of(EntityAxle::new, AXLE.get()).build(null)
    );

    public static void register(IEventBus modBus) {
        registerBlockItem("axle", AXLE);

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

}
