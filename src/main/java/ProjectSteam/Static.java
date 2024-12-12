package ProjectSteam;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class Static {

    // the order in that you define it is very important!
    // unlike older versions, the order is not linked to .addvertex(...) but has to be like this
    // I dont know why but this is the only way it works
    public static final VertexFormat POSITION_COLOR_TEXTURE_NORMAL_LIGHT =
            VertexFormat.builder()
                    .add("Position", VertexFormatElement.POSITION)
                    .add("Color", VertexFormatElement.COLOR)
                    .add("UV0", VertexFormatElement.UV0)
                    .add("UV1", VertexFormatElement.UV1)
                    .add("UV2", VertexFormatElement.UV2)
                    .add("Normal", VertexFormatElement.NORMAL)
                    .build();

    public static ShaderInstance ENTITY_SOLID_SHADER_CLONE_WITH_DYNAMIC_NORMAL;
    public static ShaderInstance getEntitySolidDynamicNormalShader(){return ENTITY_SOLID_SHADER_CLONE_WITH_DYNAMIC_NORMAL;}


    public static SoundEvent[] WOODEN_SOUNDS = {
            SoundEvents.WOODEN_BUTTON_CLICK_ON,
            SoundEvents.WOODEN_BUTTON_CLICK_OFF,
            SoundEvents.WOOD_HIT,
            SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.HORSE_STEP_WOOD,
            SoundEvents.NOTE_BLOCK_HAT.value(),
            SoundEvents.DISPENSER_LAUNCH
    };
}
