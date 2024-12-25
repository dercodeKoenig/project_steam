package ARLib;


import ARLib.blockentities.*;
import ARLib.blocks.*;
import ARLib.holoProjector.BlockStructurePreviewBlock;
import ARLib.holoProjector.EntityStructurePreviewBlock;
import ARLib.holoProjector.RenderPreviewBlock;
import ARLib.holoProjector.itemHoloProjector;
import ARLib.multiblockCore.BlockMultiblockPlaceholder;
import ARLib.multiblockCore.EntityMultiblockPlaceholder;
import ARLib.multiblockCore.RenderMultiblockPlaceholder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.swing.text.html.parser.Entity;
import java.util.function.Supplier;

//@Mod.EventBusSubscriber(modid = ARLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ARLibRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ARLib.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ARLib.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ARLib.MODID);

    public static void registerBlockItem(String name, Supplier<Block> b) {
        ITEMS.register(name, () -> new BlockItem(b.get(), new Item.Properties()));
    }


    public static final Supplier<Block> BLOCK_ENERGY_INPUT_BLOCK = BLOCKS.register("block_energy_input_block", () -> new BlockEnergyInputBlock(BlockBehaviour.Properties.of().strength(0.5f,2)));
    public static final Supplier<BlockEntityType<?>> ENTITY_ENERGY_INPUT_BLOCK = BLOCK_ENTITIES.register("entity_energy_input_block", () -> BlockEntityType.Builder.of(EntityEnergyInputBlock::new, BLOCK_ENERGY_INPUT_BLOCK.get()).build(null));

    public static final Supplier<Block> BLOCK_ENERGY_OUTPUT_BLOCK = BLOCKS.register("block_energy_output_block", () -> new BlockEnergyOutputBlock(BlockBehaviour.Properties.of().strength(0.5f,2)));
    public static final Supplier<BlockEntityType<?>> ENTITY_ENERGY_OUTPUT_BLOCK = BLOCK_ENTITIES.register("entity_energy_output_block", () -> BlockEntityType.Builder.of(EntityEnergyOutputBlock::new, BLOCK_ENERGY_OUTPUT_BLOCK.get()).build(null));

    public static final Supplier<Block> BLOCK_ITEM_INPUT_BLOCK = BLOCKS.register("block_item_input_block", () -> new BlockItemInputBlock(BlockBehaviour.Properties.of().strength(0.5f,2)));
    public static final Supplier<BlockEntityType<?>> ENTITY_ITEM_INPUT_BLOCK = BLOCK_ENTITIES.register("entity_item_input_block", () -> BlockEntityType.Builder.of(EntityItemInputBlock::new, BLOCK_ITEM_INPUT_BLOCK.get()).build(null));

    public static final Supplier<Block> BLOCK_ITEM_OUTPUT_BLOCK = BLOCKS.register("block_item_output_block", () -> new BlockItemOutputBlock(BlockBehaviour.Properties.of().strength(0.5f,2)));
    public static final Supplier<BlockEntityType<?>> ENTITY_ITEM_OUTPUT_BLOCK = BLOCK_ENTITIES.register("entity_item_output_block", () -> BlockEntityType.Builder.of(EntityItemOutputBlock::new, BLOCK_ITEM_OUTPUT_BLOCK.get()).build(null));

    public static final Supplier<Block> BLOCK_FLUID_INPUT_BLOCK = BLOCKS.register("block_fluid_input_block", () -> new BlockFluidInputBlock(BlockBehaviour.Properties.of().strength(0.5f,2)));
    public static final Supplier<BlockEntityType<?>> ENTITY_FLUID_INPUT_BLOCK = BLOCK_ENTITIES.register("entity_fluid_input_block", () -> BlockEntityType.Builder.of(EntityFluidInputBlock::new, BLOCK_FLUID_INPUT_BLOCK.get()).build(null));

    public static final Supplier<Block> BLOCK_FLUID_OUTPUT_BLOCK = BLOCKS.register("block_fluid_output_block", () -> new BlockFluidOutputBlock(BlockBehaviour.Properties.of().strength(0.5f,2)));
    public static final Supplier<BlockEntityType<?>> ENTITY_FLUID_OUTPUT_BLOCK = BLOCK_ENTITIES.register("entity_fluid_output_block", () -> BlockEntityType.Builder.of(EntityFluidOutputBlock::new, BLOCK_FLUID_OUTPUT_BLOCK.get()).build(null));

    public static final Supplier<Block> BLOCK_PLACEHOLDER = BLOCKS.register("block_placeholder", () -> new BlockMultiblockPlaceholder(BlockBehaviour.Properties.of().strength(0.5f,2)));
    public static final Supplier<BlockEntityType<EntityMultiblockPlaceholder>> ENTITY_PLACEHOLDER = BLOCK_ENTITIES.register("entity_placeholder", () -> BlockEntityType.Builder.of(EntityMultiblockPlaceholder::new, BLOCK_PLACEHOLDER.get()).build(null));

    public static final Supplier<Block> BLOCK_MOTOR = BLOCKS.register("block_motor_block", () -> new BlockMotor(BlockBehaviour.Properties.of().strength(2,2).noOcclusion()));

    public static final Supplier<Block> BLOCK_STRUCTURE = BLOCKS.register("block_structure_block", () -> new BlockStructureBlock(BlockBehaviour.Properties.of().strength(2,2)));

    public static final Supplier<Block> BLOCK_COIL_COPPER = BLOCKS.register("block_coilcopper", () -> new BlockCoilCopperBlock(BlockBehaviour.Properties.of().strength(2,2)));


    public static final DeferredHolder<Item, Item> ITEM_HOLOPROJECTOR = ITEMS.register("item_holo_projector",
            () -> new itemHoloProjector(new Item.Properties()));

    public static final DeferredHolder<Block, Block> BLOCK_STRUCTURE_PREVIEW = BLOCKS.register(
            "block_preview",
            () -> new BlockStructurePreviewBlock(BlockBehaviour.Properties.of().replaceable().instabreak().noOcclusion().noCollission())
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntityStructurePreviewBlock>> ENTITY_STRUCTURE_PREVIEW = BLOCK_ENTITIES.register(
            "entity_preview",
            () -> BlockEntityType.Builder.of(EntityStructurePreviewBlock::new, BLOCK_STRUCTURE_PREVIEW.get()).build(null)
    );

    public static void register(IEventBus modBus) {
        registerBlockItem("block_energy_input_block", BLOCK_ENERGY_INPUT_BLOCK);
        registerBlockItem("block_energy_output_block", BLOCK_ENERGY_OUTPUT_BLOCK);
        registerBlockItem("block_item_input_block", BLOCK_ITEM_INPUT_BLOCK);
        registerBlockItem("block_item_output_block", BLOCK_ITEM_OUTPUT_BLOCK);
        registerBlockItem("block_fluid_input_block", BLOCK_FLUID_INPUT_BLOCK);
        registerBlockItem("block_fluid_output_block", BLOCK_FLUID_OUTPUT_BLOCK);
        registerBlockItem("block_motor_block", BLOCK_MOTOR);
        registerBlockItem("block_structure_block", BLOCK_STRUCTURE);
        registerBlockItem("block_coilcopper", BLOCK_COIL_COPPER);

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
@OnlyIn(Dist.CLIENT)
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_STRUCTURE_PREVIEW.get(), RenderPreviewBlock::new);
        event.registerBlockEntityRenderer(ENTITY_PLACEHOLDER.get(), RenderMultiblockPlaceholder::new);
    }
    public static void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(BLOCK_ENERGY_INPUT_BLOCK.get());
            //e.accept(BLOCK_ENERGY_OUTPUT_BLOCK.get());
            e.accept(BLOCK_ITEM_INPUT_BLOCK.get());
            e.accept(BLOCK_ITEM_OUTPUT_BLOCK.get());
            e.accept(BLOCK_FLUID_INPUT_BLOCK.get());
            e.accept(BLOCK_FLUID_OUTPUT_BLOCK.get());
            e.accept(BLOCK_MOTOR.get());
            e.accept(BLOCK_STRUCTURE.get());
            e.accept(BLOCK_COIL_COPPER.get());
            e.accept((ITEM_HOLOPROJECTOR.get()));
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ARLibRegistry.ENTITY_ENERGY_INPUT_BLOCK.get(), (x, y) -> ((EntityEnergyInputBlock) x));
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ARLibRegistry.ENTITY_ENERGY_OUTPUT_BLOCK.get(), (x, y) -> ((EntityEnergyOutputBlock) x));
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ARLibRegistry.ENTITY_ITEM_INPUT_BLOCK.get(), (x, y) -> ((EntityItemInputBlock) x));
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ARLibRegistry.ENTITY_ITEM_OUTPUT_BLOCK.get(), (x, y) -> ((EntityItemOutputBlock) x));
        e.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ARLibRegistry.ENTITY_FLUID_INPUT_BLOCK.get(), (x, y) -> ((EntityFluidInputBlock) x));
        e.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ARLibRegistry.ENTITY_FLUID_OUTPUT_BLOCK.get(), (x, y) -> ((EntityFluidOutputBlock) x));
    }
}
