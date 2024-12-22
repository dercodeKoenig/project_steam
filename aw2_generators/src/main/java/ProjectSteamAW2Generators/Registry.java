package ProjectSteamAW2Generators;

import ProjectSteamAW2Generators.WaterWheel.BlockWaterWheelGenerator;
import ProjectSteamAW2Generators.WaterWheel.EntityWaterWheelGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

public class Registry {
    public static final net.neoforged.neoforge.registries.DeferredRegister<Block> BLOCKS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK, "projectsteam_aw2_generators");
    public static final net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "projectsteam_aw2_generators");
    public static final net.neoforged.neoforge.registries.DeferredRegister<Item> ITEMS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.ITEM, "projectsteam_aw2_generators");

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


    static {
        registerBlockItem("waterwheel_generator", WATERWHEEL_GENERATOR);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

}
