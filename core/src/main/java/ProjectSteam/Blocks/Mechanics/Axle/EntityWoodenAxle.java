package ProjectSteam.Blocks.Mechanics.Axle;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_WOODEN_AXLE;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenAxle extends EntityAxleBase{

    public EntityWoodenAxle(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_AXLE.get(), pos, blockState);
        super.myInertia = Config.INSTANCE.WOODEN_AXLE_INERTIA;
        super.myFriction = Config.INSTANCE.WOODEN_AXLE_FRICTION;
        super.maxStress = Config.INSTANCE.WOODEN_AXLE_MAX_STRESS;
    }

    public void tick(){
        super.tick();
        if(level.random.nextFloat() < 0.0005*Math.abs(myMechanicalBlock.internalVelocity)) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.005f*(float)Math.abs(myMechanicalBlock.internalVelocity), 1.0f);  //
        }
    }
}
