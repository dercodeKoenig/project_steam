package NPCs;

import AOSWorkshopExpansion.MillStone.ScreenMillStone;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.IOException;

import static AOSWorkshopExpansion.Registry.MENU_MILLSTONE;
import static NPCs.Registry.ENTITY_WORKER;


@Mod(NPCs.MODID)
public class NPCs {

    public static final String MODID = "aw_npc";

    public NPCs(IEventBus modEventBus, ModContainer modContaine) throws IOException {
        //modEventBus.register(this);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerNetworkStuff);
        modEventBus.addListener(this::entityAttributeCreation);
        Registry.register(modEventBus);

    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ENTITY_WORKER.get(),WorkerNPCRenderer::new);
    }

    public void registerNetworkStuff(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
    }

    public void entityAttributeCreation(EntityAttributeCreationEvent event) {
        // Register attributes for your custom entity
        event.put(ENTITY_WORKER.get(), WorkerNPC.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent e) {
        if (e.getTab().equals(Registry.CREATIVETAB.get())) {

        }
    }
}