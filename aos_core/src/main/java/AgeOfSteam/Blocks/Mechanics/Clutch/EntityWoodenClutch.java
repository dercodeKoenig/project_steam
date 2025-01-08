package AgeOfSteam.Blocks.Mechanics.Clutch;

import AgeOfSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static AgeOfSteam.Registry.ENTITY_CLUTCH;
import static AgeOfSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenClutch extends EntityClutchBase {
    public EntityWoodenClutch(BlockPos pos, BlockState blockState) {
        super(ENTITY_CLUTCH.get(), pos, blockState);

        super.inertiaPerSide = Config.INSTANCE.wooden_clutch_inertia_per_side;
        super.baseFrictionPerSide = Config.INSTANCE.wooden_clutch_friction_per_side;
        super.maxStress = Config.INSTANCE.wooden_clutch_max_stress;
        super.maxForce = Config.INSTANCE.wooden_clutch_max_force;
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
