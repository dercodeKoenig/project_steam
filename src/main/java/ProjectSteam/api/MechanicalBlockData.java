package ProjectSteam.api;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MechanicalBlockData {

BlockEntity me;
    public MechanicalBlockData(BlockEntity me){
        this.me = me;
    }

    // a client can choose what part he will track as master so no the entire structure has to be
    // loaded on the client side. only the server must have the entire network loaded
    // clients periodically send if they track a block as master and the server will update them
    // with this blocks internal velocity
    public Map<UUID, Integer> clientsTrackingThisAsMaster = new HashMap<>();
    public int cttam_timeout = 100;
    public int lastPing = 999999;


    public Map<Direction, IMechanicalBlock> connectedParts = new HashMap<>();

    boolean hasReceivedUpdate;

    public double currentRotation;

    public double internalVelocity;
    public double last_internalVelocity;

}
