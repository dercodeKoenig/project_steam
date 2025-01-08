package AgeOfSteam.Blocks.Mechanics.TJunction;

import AgeOfSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static AgeOfSteam.Registry.ENTITY_WOODEN_TJUNCTION;
import static AgeOfSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenTJuntion extends EntityTJunctionBase {
    public EntityWoodenTJuntion(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_TJUNCTION.get(),pos, blockState);

        maxStress = Config.INSTANCE.wooden_t_junction_max_stress;
        myInertia = Config.INSTANCE.wooden_t_junction_inertia;
        myFriction = Config.INSTANCE.wooden_t_junction_friction;
    }

    @Override
    public void tick(){
        super.tick();
        if(level.random.nextFloat() < 0.005*Math.abs(myMechanicalBlock.internalVelocity)) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.005f*(float)Math.abs(myMechanicalBlock.internalVelocity), 1.0f);  //
        }
    }
}
