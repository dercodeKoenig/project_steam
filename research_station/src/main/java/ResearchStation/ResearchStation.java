package ResearchStation;

import ResearchStation.Config.ResearchConfig;
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
import static ResearchStation.Registry.*;

@Mod("research_station")
public class ResearchStation {

    public ResearchStation(IEventBus modEventBus, ModContainer modContaine) throws IOException {

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
    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        ResearchConfig.PacketConfigSync.register(registrar);
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent login){
        if(login.getEntity() instanceof ServerPlayer p){
            ResearchConfig.INSTANCE.SyncConfig(p);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTab().equals(PROJECTSTEAM_CREATIVETAB.get())) {
            e.accept(RESEARCH_STATION.get());
            e.accept(ITEM_RESEARCH_BOOK.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
    }


    public void registerCapabilities(RegisterCapabilitiesEvent e) {
    }

    private void loadComplete(FMLLoadCompleteEvent e) {
    }
}