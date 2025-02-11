package NPCs;

import ARLib.utils.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class Utils {


    public static final int SUCCESS_STILL_RUNNING = 0;
    public static final int EXIT_SUCCESS = 1;
    public static final int EXIT_FAIL = -1;


    public static TreeSet<Vec3> sortByDistanceTo(Collection<Vec3> list, Vec3 position) {
        TreeSet<Vec3> sorted = new TreeSet<>(new Comparator<Vec3>() {
            @Override
            public int compare(Vec3 o1, Vec3 o2) {
                return (int) Math.signum(o1.distanceTo(position) - o2.distanceTo(position));
            }
        });
        sorted.addAll(list);
        return sorted;
    }

    public static TreeSet<BlockPos> sortBlockPosByDistanceToNPC(Collection<BlockPos> list, Vec3 position) {
        TreeSet<BlockPos> sorted = new TreeSet<>(new Comparator<BlockPos>() {
            @Override
            public int compare(BlockPos o1, BlockPos o2) {
                double d1 = o1.getCenter().distanceTo(position);
                double d2 = o2.getCenter().distanceTo(position);
                if (d1 > d2) return 1;
                if (d1 < d2) return -1;
                else {
                    if(o1.getY() != o2.getY()) return (int) Math.signum(o1.getY() - o2.getY());
                    else if(o1.getX() != o2.getX()) return (int) Math.signum(o1.getX() - o2.getX());
                    else if(o1.getZ() != o2.getZ()) return (int) Math.signum(o1.getZ() - o2.getZ());
                }
                return 0;
            }
        });
        sorted.addAll(list);
        return sorted;
    }
    public static TreeSet<BlockPos> sortBlockPosByDistanceToNPC(Collection<BlockPos> list, Entity e) {
        Vec3 position = e.getPosition(0);
        return sortBlockPosByDistanceToNPC(list,position);
    }

    public static boolean itemStacksEqual(ItemStack s1, ItemStack s2){
        return ItemStack.isSameItemSameComponents(s1,s2) && s1.getCount() == s2.getCount();
    }
    public static InteractionHand moveItemStackToAnyHand(ItemStack stack, NPCBase npc) {

        if(itemStacksEqual( stack, npc.getMainHandItem())){
            return InteractionHand.MAIN_HAND;
        }
        if(itemStacksEqual( stack, npc.getOffhandItem())){
            return InteractionHand.OFF_HAND;
        }

        if(npc.getMainHandItem().isEmpty()) {
            moveItemStackToMainHand(stack, npc);
            return InteractionHand.MAIN_HAND;
        }
        else if(npc.getOffhandItem().isEmpty()) {
            moveItemStackToOffHand(stack, npc);
            return InteractionHand.OFF_HAND;
        }
        else{
            moveItemStackToOffHand(npc.getMainHandItem(), npc);
            moveItemStackToMainHand(stack, npc);

            return InteractionHand.MAIN_HAND;
        }

    }

    public static void moveItemStackToMainHand(ItemStack stack, NPCBase npc) {
        ItemStack stackInMainHand = npc.getMainHandItem();
        if (itemStacksEqual(stack, stackInMainHand)) {
            return;
        }
        for (int i = 0; i < npc.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = npc.combinedInventory.getStackInSlot(i);
            if (itemStacksEqual(stack, stackInSlot)) {
                ItemStack tmp = stackInSlot.copy();
                npc.combinedInventory.setStackInSlot(i, stackInMainHand.copy());
                npc.setItemInHand(InteractionHand.MAIN_HAND, tmp);
                break;
            }
        }
    }
    public static void moveItemStackToOffHand(ItemStack stack, NPCBase npc) {
        ItemStack stackInOffHand = npc.getOffhandItem();
        if (itemStacksEqual(stack, stackInOffHand))
            return;

        for (int i = 0; i < npc.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = npc.combinedInventory.getStackInSlot(i);
            if (itemStacksEqual(stack, stackInSlot)) {
                ItemStack tmp = stackInSlot.copy();
                npc.combinedInventory.setStackInSlot(i, stackInOffHand.copy());
                npc.setItemInHand(InteractionHand.OFF_HAND, tmp);
                break;
            }
        }
    }
    public static int countEmptySlots(NPCBase npc) {
        int numEmptySlots = 0;
        boolean hadFoundInput = false;
        for (int i = 0; i < npc.combinedInventory.getSlots(); i++) {
            if (npc.combinedInventory.getStackInSlot(i).isEmpty()) {
                numEmptySlots++;
            }
        }
        return numEmptySlots;
    }
    public static int countItemsMatchingId(String id, IItemHandler inventory){
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (ItemUtils.matches(id,stackInSlot)) {
                count += stackInSlot.getCount();
            }
        }
        return count;
    }
    public static int countItems(Item item, IItemHandler inventory){
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot.getItem().equals(item)) {
                count += stackInSlot.getCount();
            }
        }
        return count;
    }


    public static void damageMainHandItem(NPCBase npc) {
        npc.getMainHandItem().setDamageValue(npc.getMainHandItem().getDamageValue() + 1);
        if (npc.getMainHandItem().getDamageValue() >= npc.getMainHandItem().getMaxDamage())
            npc.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    public static double distanceManhattan(Entity e, Vec3 p2) {
        return Math.abs(e.position().x - p2.x) +
                Math.abs(e.position().y - p2.y) +
                Math.abs(e.position().z - p2.z);
    }
    public static double distanceManhattan(Vec3 p1, Vec3 p2) {
        return Math.abs(p1.x - p2.x) +
                Math.abs(p1.y - p2.y) +
                Math.abs(p1.z - p2.z);
    }


    public static CompoundTag getStackTagOrEmpty(ItemStack stack) {
        try {
            return stack.get(DataComponents.CUSTOM_DATA).copyTag();
        } catch (Exception e) {
            CompoundTag itemTag = new CompoundTag();
            return itemTag;
        }
    }

    public static void setStackTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

}
