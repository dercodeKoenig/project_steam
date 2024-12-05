package ProjectSteam.api;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;

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
     **/
    void propagateTick(boolean isMasterTick);

    /**
     * The master will request the MechanicalData for its connected parts and it will be propagated through the network
     * Every part can add its own data to it and has to transform the data to the match the side it came from
     * for example, an axle connected to an axle can simply add its own data to it.
     * a gearbox may need to invert the combinedForce element. This is what the direction is for.
     * It is required to check for if you already answered the call once during this tick to avoid endless
     * recursion in case of a loop in the connections. use a simple boolean for it and reset it in any other stage of the tick sequence, for example in propagateTick()
     */
    void getPropagatedData(MechanicalData data, @Nullable Direction requestedFrom);


    /**
     * The master will after the collection of MechanicalData calculate the rotation it will output
     * to is connected parts. Every part will receive the rotation and has to propagate the rotation
     * to its connected blocks. The Parts have to transform the rotation for example in case of gear-reduction.
     *
     * If a block receives during one tick 2 different rotations, the gearing is broken/invalid
     * you need to return false if this happens. you can check if the last propagatedRotation is more than 1% different
     * from the previous received one to avoid numerical problems because you can not compare double for equality.
     *
     * You can reset the last received rotation in any other tick stage
     */
    boolean propagateRotation(double rotation);

    /**
     * to check if the block south to me (z+1) is connected to me i will ask him connectsAtFace(NORTH)
     **/
    boolean connectsAtFace(Direction otherFace);
}
