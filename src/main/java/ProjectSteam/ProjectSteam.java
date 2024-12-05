package ProjectSteam;

import ProjectSteam.Blocks.Axle.RenderAxle;
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
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_AXLE.get(), RenderAxle::new);
    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(AXLE.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
        try {
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