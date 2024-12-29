package ProjectSteamCrafting;


import ProjectSteamCrafting.Sieve.RenderSieve;
import ProjectSteamCrafting.Sieve.SieveConfig;
import ProjectSteamCrafting.SpinningWheel.RenderSpinningWheel;
import ProjectSteamCrafting.WoodMill.RenderWoodMill;
import ResearchSystem.Config.RecipeConfig;
import ResearchSystem.Config.ResearchConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
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
import static ProjectSteamCrafting.Registry.*;


@Mod("projectsteam_crafting")
public class ProjectSteamCrafting {

    public ProjectSteamCrafting(IEventBus modEventBus, ModContainer modContaine) throws IOException {
        //modEventBus.register(this);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);

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
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent login){
        if(login.getEntity() instanceof ServerPlayer p){
            SieveConfig.INSTANCE.SyncConfig(p);
        }
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_SIEVE.get(), RenderSieve::new);
        event.registerBlockEntityRenderer(ENTITY_WOODMILL.get(), RenderWoodMill::new);
        event.registerBlockEntityRenderer(ENTITY_SPINNING_WHEEL.get(), RenderSpinningWheel::new);

    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        SieveConfig.PacketConfigSync.register(registrar);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTab().equals(PROJECTSTEAM_CREATIVETAB.get())) {
            e.accept(SIEVE.get());
            e.accept(STRING_MESH.get());
            e.accept(SIEVE_HOPPER_UPGRADE.get());


            e.accept(SPINNING_WHEEL.get());


            e.accept(WOODMILL.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
    }

    private void RegisterCapabilities(RegisterCapabilitiesEvent e) {
    }

    private void loadComplete(FMLLoadCompleteEvent e) {
    }
}