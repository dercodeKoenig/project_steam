package ProjectSteam.Blocks.Mechanics.Clutch;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_CLUTCH;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenClutch extends EntityClutchBase {
    public EntityWoodenClutch(BlockPos pos, BlockState blockState) {
        super(ENTITY_CLUTCH.get(), pos, blockState);

        super.inertiaPerSide = Config.INSTANCE.WOODEN_CLUTCH_INERTIA_PER_SIDE;
        super.baseFrictionPerSide = Config.INSTANCE.WOODEN_CLUTCH_FRICTION_PER_SIDE;
        super.maxStress = Config.INSTANCE.WOODEN_CLUTCH_MAX_STRESS;
        super.maxForce = Config.INSTANCE.WOODEN_CLUTCH_MAX_FORCE;
    }

    @Override
    public void tick(){
        super.tick();
        if(level.random.nextFloat() < 0.005*(Math.abs(myMechanicalBlockA.internalVelocity)+Math.abs(myMechanicalBlockB.internalVelocity))) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.005f*(float)((Math.abs(myMechanicalBlockA.internalVelocity)+Math.abs(myMechanicalBlockB.internalVelocity))), 1.0f);  //
        }
    }
}
