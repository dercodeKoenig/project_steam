package FiniteWater;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.util.Map;

@Mod("finite_water")
public class Main {

    public Main() {
        MinecraftForge.EVENT_BUS.addListener(Main::onSourceCreate);
    }

    public static void onSourceCreate(BlockEvent.CreateFluidSourceEvent e) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            if (e.getState().getFluidState().getType().isSame(Fluids.WATER)) {
                Holder<Biome> h = e.getLevel().getBiome(e.getPos());
                ResourceLocation id = server.registryAccess().registryOrThrow(Registries.BIOME).getKey(h.value());
                String idString = id.toString();
                if (Config.INSTANCE.biomes.contains(idString)) {
                    if (Config.INSTANCE.isBlackList) {
                        e.setResult(Event.Result.DENY);
                    }
                } else {
                    if (!Config.INSTANCE.isBlackList) {
                        e.setResult(Event.Result.DENY);
                    }
                }
            }
        }
    }
}