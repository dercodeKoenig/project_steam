package ProjectSteam;

import ProjectSteam.Blocks.SimpleBlocks.BlockCasing;
import ProjectSteam.Blocks.mechanics.Axle.*;
import ProjectSteam.Blocks.mechanics.BlockMotor.BlockMotor;
import ProjectSteam.Blocks.mechanics.BlockMotor.EntityMotor;
import ProjectSteam.Blocks.mechanics.Clutch.BlockClutch;
import ProjectSteam.Blocks.mechanics.Clutch.EntityClutch;
import ProjectSteam.Blocks.mechanics.DistributorGearbox.BlockDistributorGearbox;
import ProjectSteam.Blocks.mechanics.DistributorGearbox.EntityDistributorGearbox;
import ProjectSteam.Blocks.mechanics.Gearbox.BlockGearbox;
import ProjectSteam.Blocks.mechanics.Gearbox.EntityGearbox;
import ProjectSteam.Blocks.mechanics.HandGenerator.BlockHandGenerator;
import ProjectSteam.Blocks.mechanics.HandGenerator.EntityHandGenerator;
import ProjectSteam.Blocks.mechanics.TJunction.EntityTJunction;
import ProjectSteam.Blocks.mechanics.TJunction.BlockTJunction;
import ProjectSteam.Items.Hammer.ItemHammer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Registry {
    public static final net.neoforged.neoforge.registries.DeferredRegister<Block> BLOCKS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK, "projectsteam");
    public static final net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "projectsteam");
    public static final net.neoforged.neoforge.registries.DeferredRegister<Item> ITEMS = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.ITEM, "projectsteam");

    public static final net.neoforged.neoforge.registries.DeferredRegister<CreativeModeTab> CREATIVE_TAB = net.neoforged.neoforge.registries.DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, "projectsteam");

    public static Supplier<Item> registerBlockItem(String name, Supplier<Block> b){
        return ITEMS.register(name,() -> new BlockItem(b.get(), new Item.Properties()));
    }

    public static final Supplier<Block> AXLE = BLOCKS.register(
            "axle",
            () -> new BlockWoodenAxle()
    );
    public static final Supplier<BlockEntityType<EntityWoodenAxle>> ENTITY_AXLE = BLOCK_ENTITIES.register(
            "entity_axle",
            () -> BlockEntityType.Builder.of(EntityWoodenAxle::new, AXLE.get()).build(null)
    );

    public static final Supplier<Block> AXLE_FLYWHEEL = BLOCKS.register(
            "axle_flywheel",
            () -> new BlockWoodenAxleFlyWheel()
    );
    public static final Supplier<BlockEntityType<EntityWoodenAxleFlyWheel>> ENTITY_AXLE_FLYWHEEL = BLOCK_ENTITIES.register(
            "entity_axle_flywheel",
            () -> BlockEntityType.Builder.of(EntityWoodenAxleFlyWheel::new, AXLE_FLYWHEEL.get()).build(null)
    );

    public static final Supplier<Block> AXLE_CRANKSHAFT = BLOCKS.register(
            "axle_crankshaft",
            () -> new BlockWoodenAxleCrankShaft()
    );
    public static final Supplier<BlockEntityType<EntityWoodenAxleCrankShaft>> ENTITY_AXLE_CRANKSHAFT = BLOCK_ENTITIES.register(
            "entity_axle_crankshaft",
            () -> BlockEntityType.Builder.of(EntityWoodenAxleCrankShaft::new, AXLE_CRANKSHAFT.get()).build(null)
    );

    public static final Supplier<Block> DISTRIBUTOR_GEARBOX = BLOCKS.register(
            "distributor_gearbox",
            () -> new BlockDistributorGearbox()
    );
    public static final Supplier<BlockEntityType<EntityDistributorGearbox>> ENTITY_DISTRIBUTOR_GEARBOX = BLOCK_ENTITIES.register(
            "entity_distributor_gearbox",
            () -> BlockEntityType.Builder.of(EntityDistributorGearbox::new, DISTRIBUTOR_GEARBOX.get()).build(null)
    );

    public static final Supplier<Block> GEARBOX = BLOCKS.register(
            "gearbox",
            () -> new BlockGearbox()
    );
    public static final Supplier<BlockEntityType<EntityGearbox>> ENTITY_GEARBOX = BLOCK_ENTITIES.register(
            "entity_gearbox",
            () -> BlockEntityType.Builder.of(EntityGearbox::new, GEARBOX.get()).build(null)
    );

    public static final Supplier<Block> MOTOR = BLOCKS.register(
            "motor",
            () -> new BlockMotor()
    );
    public static final Supplier<BlockEntityType<EntityMotor>> ENTITY_MOTOR = BLOCK_ENTITIES.register(
            "entity_motor",
            () -> BlockEntityType.Builder.of(EntityMotor::new, MOTOR.get()).build(null)
    );

    public static final Supplier<Block> CLUTCH = BLOCKS.register(
            "clutch",
            () -> new BlockClutch()
    );
    public static final Supplier<BlockEntityType<EntityClutch>> ENTITY_CLUTCH = BLOCK_ENTITIES.register(
            "entity_clutch",
            () -> BlockEntityType.Builder.of(EntityClutch::new, CLUTCH.get()).build(null)
    );

    public static final Supplier<Block> HAND_GENERATOR = BLOCKS.register(
            "hand_generator",
            () -> new BlockHandGenerator()
    );
    public static final Supplier<BlockEntityType<EntityHandGenerator>> ENTITY_HAND_GENERATOR = BLOCK_ENTITIES.register(
            "entity_hand_generator",
            () -> BlockEntityType.Builder.of(EntityHandGenerator::new, HAND_GENERATOR.get()).build(null)
    );

    public static final Supplier<Block> TJUNCTION = BLOCKS.register(
            "tjunction",
            () -> new BlockTJunction()
    );
    public static final Supplier<BlockEntityType<EntityTJunction>> ENTITY_TJUNCTION = BLOCK_ENTITIES.register(
            "entity_tjunction",
            () -> BlockEntityType.Builder.of(EntityTJunction::new, TJUNCTION.get()).build(null)
    );


    public static final Supplier<Block> CASING = BLOCKS.register(
            "casing",
            () -> new BlockCasing()
    );


    public static final Supplier<Item> ITEM_WOODEN_HAMMER = ITEMS.register(
            "wooden_hammer",
            () ->new ItemHammer()
    );
    public static final Supplier<Item> ITEM_WOODEN_GEAR = ITEMS.register(
            "wooden_gear",
            () ->new Item(new Item.Properties())
    );


    public static final Supplier<CreativeModeTab> PROJECTSTEAM_CREATIVETAB = CREATIVE_TAB.register(
            "age_of_steam",()->new CustomCreativeTab()
    );

    static {
        registerBlockItem("axle", AXLE);
        registerBlockItem("axle_flywheel", AXLE_FLYWHEEL);
        registerBlockItem("axle_crankshaft", AXLE_CRANKSHAFT);
        registerBlockItem("distributor_gearbox", DISTRIBUTOR_GEARBOX);
        registerBlockItem("gearbox", GEARBOX);
        registerBlockItem("motor", MOTOR);
        registerBlockItem("clutch", CLUTCH);
        registerBlockItem("hand_generator", HAND_GENERATOR);
        registerBlockItem("tjunction", TJUNCTION);

        registerBlockItem("casing", CASING);
    }

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "projectsteam");
    public static final Supplier<SoundEvent> SOUND_MOTOR = SOUND_EVENTS.register(
            "motor",
            // Takes in the registry name
            SoundEvent::createVariableRangeEvent
    );

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
        CREATIVE_TAB.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

}
