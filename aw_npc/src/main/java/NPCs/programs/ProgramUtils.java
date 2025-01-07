package NPCs.programs;

import NPCs.WorkerNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class ProgramUtils {
    public static TreeSet<BlockPos> sortBlockPosByDistanceToWorkerNPC(Collection<BlockPos> list, WorkerNPC worker) {
        Vec3 position = worker.getPosition(0);
        TreeSet<BlockPos> sorted = new TreeSet<>(new Comparator<BlockPos>() {
            @Override
            public int compare(BlockPos o1, BlockPos o2) {
                double d1 = o1.getCenter().distanceTo(position);
                double d2 = o2.getCenter().distanceTo(position);
                if (d1 > d2) return 1;
                if (d1 < d2) return -1;
                else return 0;
            }
        });
        sorted.addAll(list);
        return sorted;
    }

    public static boolean itemStacksEqual(ItemStack s1, ItemStack s2){
        return ItemStack.isSameItemSameComponents(s1,s2) && s1.getCount() == s2.getCount();
    }
    public static InteractionHand moveItemStackToAnyHand(ItemStack stack, WorkerNPC worker) {

        if(itemStacksEqual( stack, worker.getMainHandItem())){
            return InteractionHand.MAIN_HAND;
        }
        if(itemStacksEqual( stack, worker.getOffhandItem())){
            return InteractionHand.OFF_HAND;
        }

        if(worker.getMainHandItem().isEmpty()) {
            moveItemStackToMainHand(stack, worker);
            return InteractionHand.MAIN_HAND;
        }
        else if(worker.getOffhandItem().isEmpty()) {
            moveItemStackToOffHand(stack, worker);
            return InteractionHand.OFF_HAND;
        }
        else{
            moveItemStackToOffHand(worker.getMainHandItem(), worker);
            moveItemStackToMainHand(stack, worker);

            return InteractionHand.MAIN_HAND;
        }

    }

    public static void moveItemStackToMainHand(ItemStack stack, WorkerNPC worker) {
        ItemStack stackInMainHand = worker.getMainHandItem();
        if (itemStacksEqual(stack, stackInMainHand)) {
            return;
        }
        for (int i = 0; i < worker.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = worker.combinedInventory.getStackInSlot(i);
            if (itemStacksEqual(stack, stackInSlot)) {
                ItemStack tmp = stackInSlot.copy();
                worker.combinedInventory.setStackInSlot(i, stackInMainHand.copy());
                worker.setItemInHand(InteractionHand.MAIN_HAND, tmp);
                break;
            }
        }
    }
    public static void moveItemStackToOffHand(ItemStack stack, WorkerNPC worker) {
        ItemStack stackInOffHand = worker.getOffhandItem();
        if (itemStacksEqual(stack, stackInOffHand))
            return;

        for (int i = 0; i < worker.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = worker.combinedInventory.getStackInSlot(i);
            if (itemStacksEqual(stack, stackInSlot)) {
                ItemStack tmp = stackInSlot.copy();
                worker.combinedInventory.setStackInSlot(i, stackInOffHand.copy());
                worker.setItemInHand(InteractionHand.OFF_HAND, tmp);
                break;
            }
        }
    }


    public static void damageMainHandItem(WorkerNPC worker) {
        worker.getMainHandItem().setDamageValue(worker.getMainHandItem().getDamageValue() + 1);
        if (worker.getMainHandItem().getDamageValue() >= worker.getMainHandItem().getMaxDamage())
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    public static double distanceToSqr(BlockPos target, WorkerNPC worker) {
        return worker.getPosition(0).distanceToSqr(target.getCenter());
    }

    public static double distanceManhattan(BlockPos p1, BlockPos p2) {
        return Math.abs(p1.getX() - p2.getX()) +
                Math.abs(p1.getY() - p2.getY()) +
                Math.abs(p1.getZ() - p2.getZ());
    }

    public static double distanceManhattan(Entity e, BlockPos p2) {
        return Math.abs(e.position().x - p2.getX()) +
                Math.abs(e.position().y - p2.getY()) +
                Math.abs(e.position().z - p2.getZ());
    }

}
