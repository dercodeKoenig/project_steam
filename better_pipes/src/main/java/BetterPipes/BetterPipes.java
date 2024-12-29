package BetterPipes;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static BetterPipes.Registry.*;

@Mod("betterpipes")
public class BetterPipes {

    public BetterPipes() throws IOException {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::RegisterCapabilities);
        modEventBus.addListener(this::registerEntityRenderers);
        Registry.register(modEventBus);


        Path configDir = Paths.get(FMLPaths.CONFIGDIR.get().toString()); // Replace with your directory path
        String filename = "Better_Pipes.txt";

        ConfigManager configManager = new ConfigManager(configDir, filename);

        EntityPipe.MAX_OUTPUT_RATE = configManager.getInt("MAX_OUTPUT_RATE", 0);
        EntityPipe.REQUIRED_FILL_FOR_MAX_OUTPUT = configManager.getInt("MAIN_REQUIRED_FILL_FOR_MAX_OUTPUT", 0);
        EntityPipe.MAIN_CAPACITY = configManager.getInt("MAIN_CAPACITY", 0);
        EntityPipe.CONNECTION_MAX_OUTPUT_RATE = configManager.getInt("MAX_OUTPUT_RATE", 0);
        EntityPipe.CONNECTION_REQUIRED_FILL_FOR_MAX_OUTPUT = configManager.getInt("CONNECTION_REQUIRED_FILL_FOR_MAX_OUTPUT", 0);
        EntityPipe.CONNECTION_CAPACITY = configManager.getInt("CONNECTION_CAPACITY", 0);
        EntityPipe.STATE_UPDATE_TICKS = configManager.getInt("Z_STATE_UPDATE_TICKS", 0);
        EntityPipe.FORCE_OUTPUT_AFTER_TICKS = configManager.getInt("Z_FORCE_OUTPUT_AFTER_TICKS", 0);


    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        Channel.register();
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(PIPE.get(), RenderType.cutout());
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ENTITY_PIPE.get(), RenderPipe::new);
    }


    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(PIPE.get());
        }
    }

    private void RegisterCapabilities(RegisterCapabilitiesEvent e) {
        e.register(EntityPipe.class);
    }

    private void loadComplete(FMLLoadCompleteEvent e) {

    }
}