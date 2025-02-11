package AOSWorkshopExpansion;


import ARLib.holoProjector.itemHoloProjector;
import AOSWorkshopExpansion.MillStone.EntityMillStone;
import AOSWorkshopExpansion.MillStone.MillStoneConfig;
import AOSWorkshopExpansion.MillStone.RenderMillStone;
import AOSWorkshopExpansion.MillStone.ScreenMillStone;
import AOSWorkshopExpansion.Sieve.RenderSieve;
import AOSWorkshopExpansion.Sieve.SieveConfig;
import AOSWorkshopExpansion.SpinningWheel.RenderSpinningWheel;
import AOSWorkshopExpansion.SpinningWheel.SpinningWheelConfig;
import AOSWorkshopExpansion.WoodMill.EntityWoodMill;
import AOSWorkshopExpansion.WoodMill.RenderWoodMill;
import AOSWorkshopExpansion.WoodMill.WoodMillConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.IOException;

import static AOSWorkshopExpansion.Registry.*;
import static AgeOfSteam.Registry.AOS_CREATIVETAB;


@Mod(Main.MODID)
public class Main {

    public static final String MODID = "aos_workshop_expansion";

    public Main(IEventBus modEventBus, ModContainer modContaine) throws IOException {
        //modEventBus.register(this);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::RegisterCapabilities);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::loadShaders);
        modEventBus.addListener(this::registerNetworkStuff);
        modEventBus.addListener(this::registerScreens);
        Registry.register(modEventBus);
    }

    public void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MENU_MILLSTONE.get(), ScreenMillStone::new);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent login) {
        if (login.getEntity() instanceof ServerPlayer p) {
            SieveConfig.INSTANCE.SyncConfig(p);
            SpinningWheelConfig.INSTANCE.SyncConfig(p);
            WoodMillConfig.INSTANCE.SyncConfig(p);
            MillStoneConfig.INSTANCE.SyncConfig(p);
        }
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_SIEVE.get(), RenderSieve::new);
        event.registerBlockEntityRenderer(ENTITY_WOODMILL.get(), RenderWoodMill::new);
        event.registerBlockEntityRenderer(ENTITY_SPINNING_WHEEL.get(), RenderSpinningWheel::new);
        event.registerBlockEntityRenderer(ENTITY_MILLSTONE.get(), RenderMillStone::new);
    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        SieveConfig.PacketConfigSync.register(registrar);
        SpinningWheelConfig.PacketConfigSync.register(registrar);
        WoodMillConfig.PacketConfigSync.register(registrar);
        MillStoneConfig.PacketConfigSync.register(registrar);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTab().equals(AOS_CREATIVETAB.get())) {
            e.accept(SIEVE.get());
            e.accept(STRING_MESH.get());
            e.accept(SIEVE_HOPPER_UPGRADE.get());


            e.accept(SPINNING_WHEEL.get());


            e.accept(WOODMILL.get());


            e.accept(MILLSTONE.get());
            e.accept(FLOUR.get());
        }
    }

    private void loadShaders(RegisterShadersEvent e) {
    }

    private void RegisterCapabilities(RegisterCapabilitiesEvent e) {
    }

    private void loadComplete(FMLLoadCompleteEvent e) {
        itemHoloProjector.registerMultiblock("MillStone", EntityMillStone.structure, EntityMillStone.charMapping);
        itemHoloProjector.registerMultiblock("WoodMill", EntityWoodMill.structure, EntityWoodMill.charMapping);
    }
}