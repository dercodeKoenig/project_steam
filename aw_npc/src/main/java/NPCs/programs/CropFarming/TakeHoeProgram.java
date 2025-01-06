package NPCs.programs.CropFarming;

import Farms.CropFarm.EntityCropFarm;
import NPCs.WorkerNPC;
import NPCs.programs.CropFarmingProgram;
import NPCs.programs.ExitCode;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

public class TakeHoeProgram {

    CropFarmingProgram parentProgram;
    int workDelay = 0;

    public TakeHoeProgram(CropFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }

    public static boolean canPickupHoeFromFarm(EntityCropFarm farm, WorkerNPC worker) {
        if (!worker.getMainHandItem().isEmpty()) return false;
        for (int j = 0; j < farm.mainInventory.getSlots(); j++) {
            ItemStack stackInSlot = farm.mainInventory.getStackInSlot(j);
            if (stackInSlot.getItem() instanceof HoeItem) {
                return true;
            }
        }
        return false;
    }

    public ExitCode run() {
        if (!canPickupHoeFromFarm(parentProgram.currentFarm, parentProgram.worker)) {
            return ExitCode.EXIT_SUCCESS;
        }

        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, parentProgram.currentFarm.getBlockPos().getCenter());
        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, parentProgram.currentFarm.getBlockPos().getCenter());

        ExitCode pathFindExit = parentProgram.moveNearFarm(3);
        if (pathFindExit.isFailed())
            return ExitCode.EXIT_FAIL;
        else if (pathFindExit.isStillRunning()) {
            workDelay = 0;
            return ExitCode.SUCCESS_STILL_RUNNING;
        }

        if (workDelay > 20) {
            workDelay = 0;
            for (int j = 0; j < parentProgram.currentFarm.mainInventory.getSlots(); j++) {
                ItemStack stackInSlot = parentProgram.currentFarm.mainInventory.getStackInSlot(j);
                if (stackInSlot.getItem() instanceof HoeItem) {
                    parentProgram.worker.setItemInHand(InteractionHand.MAIN_HAND, stackInSlot.copy());
                    parentProgram.currentFarm.mainInventory.setStackInSlot(j, ItemStack.EMPTY);
                    parentProgram.currentFarm.setChanged();
                    parentProgram.worker.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
        workDelay++;
        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
