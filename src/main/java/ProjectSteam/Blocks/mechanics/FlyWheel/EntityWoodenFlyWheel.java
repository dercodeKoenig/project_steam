package ProjectSteam.Blocks.mechanics.FlyWheel;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_WOODEN_FLYWHEEL;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenFlyWheel extends EntityFlyWheelBase {

    public EntityWoodenFlyWheel(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_FLYWHEEL.get(), pos, blockState);
        myInertia = 10;
        myFriction = 0.1;
        maxStress = 500;
    }

    public void tick(){
        super.tick();
        if(level.random.nextFloat() < 0.0005*Math.abs(myMechanicalBlock.internalVelocity)) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.002f*(float)Math.abs(myMechanicalBlock.internalVelocity), 1.0f);  //
        }
    }
}