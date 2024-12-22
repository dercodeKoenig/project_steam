package ProjectSteamAW2Generators;

import ProjectSteamAW2Generators.WaterWheel.RenderWaterWheelGenerator;
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

import java.io.IOException;

import static ProjectSteam.Registry.PROJECTSTEAM_CREATIVETAB;
import static ProjectSteamAW2Generators.Registry.ENTITY_WATERWHEEL_GENERATOR;
import static ProjectSteamAW2Generators.Registry.WATERWHEEL_GENERATOR;



@Mod("projectsteam_aw2_generators")
public class ProjectSteamAW2Generators {

    public ProjectSteamAW2Generators(IEventBus modEventBus, ModContainer modContaine) throws IOException {
        //modEventBus.register(this);
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
        event.registerBlockEntityRenderer(ENTITY_WATERWHEEL_GENERATOR.get(), RenderWaterWheelGenerator::new);
    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTab().equals(PROJECTSTEAM_CREATIVETAB.get())) {
            e.accept(WATERWHEEL_GENERATOR.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
    }

    private void RegisterCapabilities(RegisterCapabilitiesEvent e) {
    }

    private void loadComplete(FMLLoadCompleteEvent e) {
    }
}