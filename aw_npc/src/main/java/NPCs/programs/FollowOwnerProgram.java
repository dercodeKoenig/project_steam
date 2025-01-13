package NPCs.programs;

import NPCs.NPCBase;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

import static NPCs.programs.ProgramUtils.EXIT_FAIL;

public class FollowOwnerProgram extends Goal {
    NPCBase worker;

    public FollowOwnerProgram(NPCBase worker) {
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return worker.followOwner != null;
    }

    @Override
    public void tick() {
        if(worker.followOwner == null)return;

        if(worker.level() instanceof  ServerLevel l){
            Entity owner= l.getEntity(worker.followOwner);
            if(owner instanceof Player){
                if(worker.slowMobNavigation.moveToPosition(
                        owner.getOnPos(),
                        5,128,512,30
                ) == EXIT_FAIL){
                    worker.followOwner = null;
                }
                worker.lookAt(EntityAnchorArgument.Anchor.EYES,owner.getEyePosition());
            }else{
                worker.followOwner = null;
            }
        }
    }
}

