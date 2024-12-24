package ProjectSteamAW2Generators;

import ARLib.blockentities.*;
import ProjectSteamAW2Generators.Config.Config;
import ProjectSteamAW2Generators.Config.PacketConfigSync;
import ProjectSteamAW2Generators.StirlingGenerator.EntityStirlingGenerator;
import ProjectSteamAW2Generators.StirlingGenerator.RenderStirlingGenerator;
import ProjectSteamAW2Generators.WaterWheel.RenderWaterWheelGenerator;
import ProjectSteamAW2Generators.WindMill.RenderWindMillGenerator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

import static ProjectSteam.Registry.PROJECTSTEAM_CREATIVETAB;
import static ProjectSteamAW2Generators.Registry.*;


@Mod("projectsteam_aw2_generators")
public class ProjectSteamAW2Generators {

    public ProjectSteamAW2Generators(IEventBus modEventBus, ModContainer modContaine) throws IOException {

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
        if (e.getTab().equals(PROJECTSTEAM_CREATIVETAB.get())) {
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