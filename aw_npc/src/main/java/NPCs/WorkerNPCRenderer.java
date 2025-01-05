package NPCs;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;

public class WorkerNPCRenderer extends MobRenderer<WorkerNPC, PlayerModel<WorkerNPC>> {
    public WorkerNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WorkerNPC entity) {
        return ResourceLocation.fromNamespaceAndPath("aw_npc", "textures/entity/worker.png");
    }
}
