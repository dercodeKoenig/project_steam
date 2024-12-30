package ProjectSteam.Blocks.Mechanics.FlyWheel;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static ProjectSteam.Registry.ENTITY_WOODEN_FLYWHEEL;
import static ProjectSteam.Registry.ENTITY_WOODEN_FLYWHEEL_LARGE;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenFlyWheelLarge extends EntityFlyWheelBase {

    // used to store positions around me so that tick is faster
    List<BlockPos> positionsAroundMe = new ArrayList<>();
    Direction.Axis myLastAxis = null;

    public EntityWoodenFlyWheelLarge(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_FLYWHEEL_LARGE.get(), pos, blockState);
        myInertia = Config.INSTANCE.WOODEN_FLYWHEEL_LARGE_INERTIA;
        myFriction = Config.INSTANCE.WOODEN_FLYWHEEL_LARGE_FRICTION;
        maxStress = Config.INSTANCE.WOODEN_FLYWHEEL_LARGE_MAX_STRESS;
    }

    public void updatePositionsAroundMe() {
        positionsAroundMe.clear();
        Direction.Axis myAxis = getBlockState().getValue(BlockFlyWheelBase.ROTATION_AXIS);
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (x == 0 && y == 0) continue;
                BlockPos myPos = getBlockPos();
                BlockPos p = null;
                if (myAxis == Direction.Axis.X) {
                    p = new BlockPos(myPos.offset(0, y, x));
                }
                if (myAxis == Direction.Axis.Z) {
                    p = new BlockPos(myPos.offset(x, y, 0));
                }
                if (myAxis == Direction.Axis.Y) {
                    p = new BlockPos(myPos.offset(x, 0, y));
                }
                positionsAroundMe.add(p);
            }
        }
    }

    public void tick() {
        super.tick();
        if (level.random.nextFloat() < 0.0005 * Math.abs(myMechanicalBlock.internalVelocity)) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.005f * (float) Math.abs(myMechanicalBlock.internalVelocity), 1.0f);  //
        }
        if (!level.isClientSide) {

            // check if the block rotated, it can if you update blocks next to it
            Direction.Axis myAxis = getBlockState().getValue(BlockFlyWheelBase.ROTATION_AXIS);
            if(!myAxis.equals(myLastAxis)){
                myLastAxis = myAxis;
                updatePositionsAroundMe();
            }

            boolean blocked = false;
            for (BlockPos p : positionsAroundMe) {
                BlockState s = level.getBlockState(p);
                if (!s.isAir()) {
                    blocked = true;
                    break;
                }
            }
            if (blocked) {
                myFriction = 9000;
            } else {
                myFriction = Config.INSTANCE.WOODEN_FLYWHEEL_LARGE_FRICTION;
            }
        }
    }
}