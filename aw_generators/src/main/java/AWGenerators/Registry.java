package AWGenerators;

import AWGenerators.StirlingGenerator.BlockStirlingGenerator;
import AWGenerators.StirlingGenerator.EntityStirlingGenerator;
import AWGenerators.WaterWheel.BlockWaterWheelGenerator;
import AWGenerators.WaterWheel.EntityWaterWheelGenerator;
import AWGenerators.WindMill.BlockWindMillBlade;
import AWGenerators.WindMill.BlockWindMillGenerator;
import AWGenerators.WindMill.EntityWindMillGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

public class Registry {
    public static final net.neoforged.neoforge.registries.DeferredRegister<Block> BLOCKS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK, Main.MODID);
    public static final net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Main.MODID);
    public static final net.neoforged.neoforge.registries.DeferredRegister<Item> ITEMS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.ITEM, Main.MODID);

    public static Supplier<Item> registerBlockItem(String name, Supplier<Block> b){
        return ITEMS.register(name,() -> new BlockItem(b.get(), new Item.Properties()));
    }

    public static final Supplier<Block> WATERWHEEL_GENERATOR = BLOCKS.register(
            "waterwheel_generator",
            () -> new BlockWaterWheelGenerator()
    );
    public static final Supplier<BlockEntityType<EntityWaterWheelGenerator>> ENTITY_WATERWHEEL_GENERATOR = BLOCK_ENTITIES.register(
            "entity_waterwheel_generator",
            () -> BlockEntityType.Builder.of(EntityWaterWheelGenerator::new, WATERWHEEL_GENERATOR.get()).build(null)
    );

    public static final Supplier<Block> WINDMILL_GENERATOR = BLOCKS.register(
            "windmill_generator",
            () -> new BlockWindMillGenerator()
    );
    public static final Supplier<BlockEntityType<EntityWindMillGenerator>> ENTITY_WINDMILL_GENERATOR = BLOCK_ENTITIES.register(
            "entity_windmill_generator",
            () -> BlockEntityType.Builder.of(EntityWindMillGenerator::new, WINDMILL_GENERATOR.get()).build(null)
    );
    public static final Supplier<Block> WINDMILL_BLADE = BLOCKS.register(
            "windmill_blade",
            () -> new BlockWindMillBlade()
    );

    public static final Supplier<Block> STIRLING_GENERATOR = BLOCKS.register(
            "stirling_generator",
            () -> new BlockStirlingGenerator()
    );
    public static final Supplier<BlockEntityType<EntityStirlingGenerator>> ENTITY_STIRLING_GENERATOR = BLOCK_ENTITIES.register(
            "entity_stirling_generator",
            () -> BlockEntityType.Builder.of(EntityStirlingGenerator::new, STIRLING_GENERATOR.get()).build(null)
    );

    static {
        registerBlockItem("waterwheel_generator", WATERWHEEL_GENERATOR);

        registerBlockItem("windmill_generator", WINDMILL_GENERATOR);
        registerBlockItem("windmill_blade", WINDMILL_BLADE);

        registerBlockItem("stirling_generator", STIRLING_GENERATOR);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

}
