package ProjectSteam;

import ARLib.ARLibRegistry;
import ARLib.blockentities.EntityEnergyInputBlock;
import ProjectSteam.Blocks.mechanics.Axle.RenderWoodenAxle;
import ProjectSteam.Blocks.mechanics.Axle.RenderWoodenAxleFlyWheel;
import ProjectSteam.Blocks.mechanics.BlockMotor.RenderMotor;
import ProjectSteam.Blocks.mechanics.DistributorGearbox.RenderDistributorGearbox;
import ProjectSteam.Blocks.mechanics.Gearbox.RenderGearbox;
import ProjectSteam.Blocks.mechanics.HandGenerator.RenderHandGenerator;
import ProjectSteam.Blocks.mechanics.TJunction.RenderTJunction;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.IOException;

import static ProjectSteam.Registry.*;
import static ProjectSteam.Static.POSITION_COLOR_TEXTURE_NORMAL_LIGHT;
import static ProjectSteam.Static.WOODEN_SOUNDS;


@Mod("projectsteam")
public class ProjectSteam {

    public ProjectSteam(IEventBus modEventBus, ModContainer modContaine) throws IOException {
        //NeoForge.EVENT_BUS.register(EntityPipe.class);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::RegisterCapabilities);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::loadShaders);
        modEventBus.addListener(this::registerNetworkStuff);
        Registry.register(modEventBus);


    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(DISTRIBUTOR_GEARBOX.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(GEARBOX.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(TJUNCTION.get(), RenderType.cutout());
    }


    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_AXLE.get(), RenderWoodenAxle::new);
        event.registerBlockEntityRenderer(ENTITY_AXLE_FLYWHEEL.get(), RenderWoodenAxleFlyWheel::new);
        event.registerBlockEntityRenderer(ENTITY_DISTRIBUTOR_GEARBOX.get(), RenderDistributorGearbox::new);
        event.registerBlockEntityRenderer(ENTITY_GEARBOX.get(), RenderGearbox::new);
        event.registerBlockEntityRenderer(ENTITY_MOTOR.get(), RenderMotor::new);
        event.registerBlockEntityRenderer(ENTITY_HAND_GENERATOR.get(), RenderHandGenerator::new);
        event.registerBlockEntityRenderer(ENTITY_TJUNCTION.get(), RenderTJunction::new);

    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == PROJECTSTEAM_CREATIVETAB.getKey()) {
            e.accept(AXLE.get());
            e.accept(AXLE_FLYWHEEL.get());
            e.accept(DISTRIBUTOR_GEARBOX.get());
            e.accept(GEARBOX.get());
            e.accept(MOTOR.get());
            e.accept(CLUTCH.get());
            e.accept(HAND_GENERATOR.get());
            e.accept(TJUNCTION.get());

            e.accept(CASING.get());
            e.accept(ITEM_WOODEN_HAMMER.get());
            e.accept(ITEM_WOODEN_GEAR.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
        try {
            /// TODO make light coords as uniforms to avoid lag on light updates
            e.registerShader(new ShaderInstance(e.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("projectsteam", "shader_dynamic_normal"),POSITION_COLOR_TEXTURE_NORMAL_LIGHT),(shader)->Static.ENTITY_SOLID_SHADER_CLONE_WITH_DYNAMIC_NORMAL = shader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void RegisterCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ENTITY_MOTOR.get(), (x, y) -> (x));
    }

    private void loadComplete(FMLLoadCompleteEvent e) {
    }
}