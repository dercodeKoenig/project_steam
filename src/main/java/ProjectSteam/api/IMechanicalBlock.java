package ProjectSteam.api;

import com.ibm.icu.impl.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import javax.annotation.Nullable;
import java.util.*;

public interface IMechanicalBlock {


    /**
     *
     *
     * The sequence is as follows:
     * - propagateTick: used to tick every BlockEntity in the network, can be used to reset flags from other stages
     * - getPropagatedData: used to get the force, friction and mass for every BlockEntity in the network, can be used to reset flags from other stages
     * - propagateRotation: used to update the rotation for every BlockEntity in the network, can be used to reset flags from other stages
     *
     * for it to work correctly every part of the network has to be chunk-loaded / every part should make sure their connected parts are loaded
     *
     *
     */


    /**
     * Forward a tick through the entire network to update all parts that need to be updated before calculations can begin
     * Every MechanicalPart that can add force to the network can call this method.
     * Whatever BlockEntity that adds force to the network receives the tick() first will be the master during this tick
     * The master will propagate the tick through all connected parts and ask all tick-able parts to tick
     * The tick-able part needs to keep track of if it already ticked during this server tick or not to not double tick
     * This can be done by setting a boolean to true in propagateTick and set it to false in the following getPropagatedData() call
     *
     * retuns false if not all the network is loaded
     **/
    boolean propagateTick();

    /**
     * The master will request the MechanicalData for its connected parts and it will be propagated through the network
     * Every part can add its own data to it and has to transform the data to the match the side it came from
     * for example, an axle connected to an axle can simply add its own data to it.
     * a gearbox may need to invert the combinedForce element. This is what the direction is for.
     * It is required to check for if you already answered the call once during this tick to avoid endless
     * recursion in case of a loop in the connections. use a simple boolean for it and reset it in any other stage of the tick sequence, for example in propagateTick()
     */
    void getPropagatedData(MechanicalData data, @Nullable Direction requestedFrom, HashSet<BlockPos> workedPositions);


    /**
     * The master will after the collection of MechanicalData calculate the rotation it will output
     * to is connected parts. Every part will receive the rotation and has to propagate the rotation
     * to its connected blocks. The Parts have to transform the rotation for example in case of gear-reduction.
     * <p>
     * If a block receives during one tick 2 different rotations, the gearing is broken/invalid
     * you need to return false if this happens. you can check if the last propagatedRotation is more than 1% different
     * from the previous received one to avoid numerical problems because you can not compare double for equality.
     * <p>
     * You can reset the last received rotation in any other tick stage
     */
    void propagateRotation(double rotation, @Nullable Direction receivingFace, HashSet<BlockPos> workedPositions);

    void propagateVelocityUpdate(double velocity, @Nullable Direction receivingFace, HashSet<BlockPos> workedPositions);


    /**
     * to check if the block south to me (z+1) is connected to me i will ask him connectsAtFace(NORTH)
     **/
    boolean connectsAtFace(Direction face, @Nullable BlockState myState);


    default double getRotationMultiplierToInside(@Nullable Direction receivingFace){
        return 1;
    }
    default     double getRotationMultiplierToOutside(@Nullable Direction outputFace){
        return 1;
    }






    /**
     will return null if any block nex to it is not loaded to avoid false updates
     to make the network work, every part of it has to be loaded. if only one part is not loaded,
     no more updates will happen
     */
    @Nullable
    default Map<Direction, IMechanicalBlock> getConnectedParts(BlockEntity mechanicalBlockBE, @Nullable BlockState myBlockState) {

        Map<Direction, IMechanicalBlock> connectedBlocks = new HashMap<>();

        if (myBlockState == null)
            myBlockState = mechanicalBlockBE.getLevel().getBlockState(mechanicalBlockBE.getBlockPos());

        for (Direction i : Direction.values()) {
            if (((IMechanicalBlock) mechanicalBlockBE).connectsAtFace(i, myBlockState)) {
                // make sure the chunk is loaded for correct calculations or return null
                if(!mechanicalBlockBE.getLevel().isLoaded(mechanicalBlockBE.getBlockPos().relative(i)))
                    return null;


                BlockEntity other = mechanicalBlockBE.getLevel().getBlockEntity(mechanicalBlockBE.getBlockPos().relative(i));
                if (other instanceof IMechanicalBlock othermechBlock && othermechBlock.connectsAtFace(i.getOpposite(), null)) {
                    connectedBlocks.put(i, othermechBlock);
                }
            }
        }
        return connectedBlocks;
    }
}
