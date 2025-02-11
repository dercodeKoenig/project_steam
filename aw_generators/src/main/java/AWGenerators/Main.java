package AWGenerators;

import AWGenerators.Config.Config;
import AWGenerators.Config.PacketConfigSync;
import AWGenerators.StirlingGenerator.EntityStirlingGenerator;
import AWGenerators.StirlingGenerator.RenderStirlingGenerator;
import AWGenerators.WaterWheel.RenderWaterWheelGenerator;
import AWGenerators.WindMill.RenderWindMillGenerator;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.IOException;

import static AWGenerators.Registry.*;
import static AgeOfSteam.Registry.AOS_CREATIVETAB;


@Mod(Main.MODID)
public class Main {

    public static final String MODID = "aw_generators";

    public Main(IEventBus modEventBus, ModContainer modContaine) throws IOException {

        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::loadShaders);
        modEventBus.addListener(this::registerNetworkStuff);
        Registry.register(modEventBus);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
    }


    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_WATERWHEEL_GENERATOR.get(), RenderWaterWheelGenerator::new);
        event.registerBlockEntityRenderer(ENTITY_WINDMILL_GENERATOR.get(), RenderWindMillGenerator::new);
        event.registerBlockEntityRenderer(ENTITY_STIRLING_GENERATOR.get(), RenderStirlingGenerator::new);
    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        PacketConfigSync.register(registrar);
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent login){
        if(login.getEntity() instanceof ServerPlayer p){
            Config.INSTANCE.SyncConfig(p);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTab().equals(AOS_CREATIVETAB.get())) {
            e.accept(WATERWHEEL_GENERATOR.get());
            e.accept(WINDMILL_GENERATOR.get());
            e.accept(WINDMILL_BLADE.get());
            e.accept(STIRLING_GENERATOR.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
    }


    public void registerCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ENTITY_STIRLING_GENERATOR.get(), (x, y) -> ((EntityStirlingGenerator)x).inventory);
    }

    private void loadComplete(FMLLoadCompleteEvent e) {
    }
}