package ProjectSteam.Blocks.Mechanics.CrankShaft;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_SMALL_WOODEN_CRANKSHAFT;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntitySmallWoodenCrankShaft extends EntityCrankShaftBase {

    public EntitySmallWoodenCrankShaft(BlockPos pos, BlockState blockState) {
        super(ICrankShaftConnector.CrankShaftType.SMALL,ENTITY_SMALL_WOODEN_CRANKSHAFT.get(), pos, blockState);

        maxStress = Config.INSTANCE.WOODEN_CRANKSHAFT_SMALL_MAX_STRESS;
        myInertia = Config.INSTANCE.WOODEN_CRANKSHAFT_SMALL_INERTIA;
        myFriction = Config.INSTANCE.WOODEN_CRANKSHAFT_SMALL_FRICTION;
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