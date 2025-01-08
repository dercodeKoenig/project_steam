package AgeOfSteam.Blocks.Mechanics.CrankShaft;

import AgeOfSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static AgeOfSteam.Registry.ENTITY_BIG_WOODEN_CRANKSHAFT;
import static AgeOfSteam.Static.WOODEN_SOUNDS;

public class EntityBigWoodenCrankShaft extends EntityCrankShaftBase {

    public EntityBigWoodenCrankShaft(BlockPos pos, BlockState blockState) {
        super(ICrankShaftConnector.CrankShaftType.LARGE,ENTITY_BIG_WOODEN_CRANKSHAFT.get(), pos, blockState);
        maxStress = Config.INSTANCE.wooden_crankshaft_big_max_stress;
        myInertia = Config.INSTANCE.wooden_crankshaft_big_inertia;
        myFriction = Config.INSTANCE.wooden_crankshaft_big_friction;
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