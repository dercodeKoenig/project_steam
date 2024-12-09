package ProjectSteam;

import ProjectSteam.Blocks.Axle.RenderAxle;
import ProjectSteam.Blocks.BlockMotor.RenderMotor;
import ProjectSteam.Blocks.DistributorGearbox.RenderDistributorGearbox;
import ProjectSteam.Blocks.Gearbox.RenderGearbox;
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
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.IOException;

import static ProjectSteam.Registry.*;
import static ProjectSteam.Static.POSITION_COLOR_TEXTURE_NORMAL_LIGHT;


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
    }


    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_AXLE.get(), RenderAxle::new);
        event.registerBlockEntityRenderer(ENTITY_DISTRIBUTOR_GEARBOX.get(), RenderDistributorGearbox::new);
        event.registerBlockEntityRenderer(ENTITY_GEARBOX.get(), RenderGearbox::new);
        event.registerBlockEntityRenderer(ENTITY_MOTOR.get(), RenderMotor::new);

    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(AXLE.get());
            e.accept(DISTRIBUTOR_GEARBOX.get());
            e.accept(GEARBOX.get());
            e.accept(MOTOR.get());
            e.accept(CLUTCH.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
        try {
            /// TODO make light coords as uniforms to avoid lag on light updates
            e.registerShader(new ShaderInstance(e.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("projectsteam", "shader_axle"),POSITION_COLOR_TEXTURE_NORMAL_LIGHT),(shader)->Static.ENTITY_SOLID_SHADER_CLONE_WITH_DYNAMIC_NORMAL = shader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void RegisterCapabilities(RegisterCapabilitiesEvent e) {
    }

    private void loadComplete(FMLLoadCompleteEvent e) {
    }
}