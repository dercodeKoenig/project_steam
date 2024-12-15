package ProjectSteam.Blocks.mechanics.CrankShaft;

import ProjectSteam.Blocks.mechanics.Axle.EntityAxleBase;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_WOODEN_CRANKSHAFT;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenCrankShaft extends EntityAxleBase {

    public EntityWoodenCrankShaft(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_CRANKSHAFT.get(), pos, blockState);
        maxStress = 100;
        myInertia = 1;
        myFriction = 0.1;
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