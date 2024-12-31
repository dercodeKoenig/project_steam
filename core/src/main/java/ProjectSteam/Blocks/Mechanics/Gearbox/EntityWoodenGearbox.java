package ProjectSteam.Blocks.Mechanics.Gearbox;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_WOODEN_GEARBOX;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenGearbox extends EntityGearboxBase{
    public EntityWoodenGearbox(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_GEARBOX.get(), pos, blockState);

        maxStress = Config.INSTANCE.wooden_gearbox_max_stress;
        myInertia = Config.INSTANCE.wooden_gearbox_inertia;
        myFriction = Config.INSTANCE.wooden_gearbox_friction;
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
