package ResearchSystem;

import ResearchSystem.EngineeringStation.BlockEngineeringStation;
import ResearchSystem.EngineeringStation.recipeConfig;
import ResearchSystem.EngineeringStation.EntityEngineeringStation;
import ResearchSystem.EngineeringStation.MenuEngineeringStation;
import ResearchSystem.ResearchStation.BlockResearchStation;
import ResearchSystem.ResearchStation.EntityResearchStation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Registry {
    public static final DeferredRegister<Block> BLOCKS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK, "research_station");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "research_station");
    public static final DeferredRegister<Item> ITEMS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.ITEM, "research_station");
    public static final DeferredRegister<MenuType<?>> MENUS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.MENU, "research_station");

    public static Supplier<Item> registerBlockItem(String name, Supplier<Block> b){
        return ITEMS.register(name,() -> new BlockItem(b.get(), new Item.Properties()));
    }

    public static final Supplier<Block> RESEARCH_STATION = BLOCKS.register(
            "research_station",
            () -> new BlockResearchStation()
    );
    public static final Supplier<BlockEntityType<EntityResearchStation>> ENTITY_RESEARCH_STATION = BLOCK_ENTITIES.register(
            "entity_research_station",
            () -> BlockEntityType.Builder.of(EntityResearchStation::new, RESEARCH_STATION.get()).build(null)
    );

    public static final Supplier<Block> ENGINEERING_STATION = BLOCKS.register(
            "engineering_station",
            () -> new BlockEngineeringStation()
    );
    public static final Supplier<BlockEntityType<EntityEngineeringStation>> ENTITY_ENGINEERING_STATION = BLOCK_ENTITIES.register(
            "entity_engineering_station",
            () -> BlockEntityType.Builder.of(EntityEngineeringStation::new, ENGINEERING_STATION.get()).build(null)
    );

    public static final Supplier<Item> ITEM_RESEARCH_BOOK = ITEMS.register(
            "research_book",
            () -> new ItemResearchBook()
    );

    public static final Supplier<MenuType<MenuEngineeringStation>> MENU_ENGINEERING_STATION = MENUS.register("menu_engineering_station", () -> new MenuType<>(MenuEngineeringStation::new, FeatureFlags.DEFAULT_FLAGS));

    static {
        registerBlockItem("research_station", RESEARCH_STATION);
        registerBlockItem("engineering_station", ENGINEERING_STATION);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
     MENUS.register(modBus);
    }

}
