package ARLib.gui;

import ARLib.gui.modules.GuiModuleBase;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.UUID;

public interface IGuiHandler {

    //void registerModule(GuiModuleBase guiModule);
    List<GuiModuleBase> getModules();

    void broadcastUpdate(CompoundTag tag);

    default void serverTick() {
        for (GuiModuleBase m : getModules()) {
            m.serverTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    void sendToServer(CompoundTag tag);

    @OnlyIn(Dist.CLIENT)
    void onGuiClientTick();

    @OnlyIn(Dist.CLIENT)
    default void onGuiClose() {}

    @OnlyIn(Dist.CLIENT)
    default void readClient(CompoundTag tag) {
        for (GuiModuleBase m : getModules()) {
            m.client_handleDataSyncedToClient(tag);
        }
    }

    default void readServer(CompoundTag tag) {
        if (tag.contains("dropItem")) {
            CompoundTag myTag = tag.getCompound("dropItem");
            boolean dropAll = myTag.getBoolean("dropAll");
            UUID playerid = myTag.getUUID("uuid_from");
            Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerid);
            if (dropAll)
                dropPlayersCarriedItem(player);
            else
                dropSinglePlayersCarriedItem(player);
        }

        for (GuiModuleBase m : getModules()) {
            m.server_readNetworkData(tag);
        }
    }

    default void dropPlayersCarriedItem(Player p) {
        ItemStack carried = p.inventoryMenu.getCarried();
        if (!carried.isEmpty()) {
            // Calculate a direction vector based on the player's current facing direction
            float yaw = p.getYRot();  // Yaw angle (horizontal rotation)
            double xDir = -Math.sin(Math.toRadians(yaw));
            double zDir = Math.cos(Math.toRadians(yaw));

            // Adjust the momentum by setting a speed multiplier (e.g., 0.3)
            double speed = 0.3;
            double xVelocity = xDir * speed;
            double yVelocity = 0.1;  // Small upward momentum to make the item "pop" up a bit
            double zVelocity = zDir * speed;

            ItemEntity itemEntity = new ItemEntity(p.level(), p.position().x, p.position().y, p.position().z, carried.copy());
            itemEntity.setPickUpDelay(40);

            // Set the velocity of the ItemEntity to give it momentum
            itemEntity.setDeltaMovement(xVelocity, yVelocity, zVelocity);

            p.level().addFreshEntity(itemEntity);
        }
        p.inventoryMenu.setCarried(ItemStack.EMPTY);
        p.inventoryMenu.broadcastChanges();
    }

    default void dropSinglePlayersCarriedItem(Player p) {
        ItemStack carried = p.inventoryMenu.getCarried();
        if (!carried.isEmpty()) {
            // Calculate a direction vector based on the player's current facing direction
            float yaw = p.getYRot();  // Yaw angle (horizontal rotation)
            double xDir = -Math.sin(Math.toRadians(yaw));
            double zDir = Math.cos(Math.toRadians(yaw));

            // Adjust the momentum by setting a speed multiplier (e.g., 0.3)
            double speed = 0.3;
            double xVelocity = xDir * speed;
            double yVelocity = 0.1;  // Small upward momentum to make the item "pop" up a bit
            double zVelocity = zDir * speed;
            ItemEntity itemEntity = new ItemEntity(p.level(), p.position().x, p.position().y, p.position().z, carried.copyWithCount(1));
            itemEntity.setPickUpDelay(40);

            // Set the velocity of the ItemEntity to give it momentum
            itemEntity.setDeltaMovement(xVelocity, yVelocity, zVelocity);

            carried.shrink(1);
            p.inventoryMenu.setCarried(carried);
            p.level().addFreshEntity(itemEntity);
            p.inventoryMenu.broadcastChanges();
        }
    }
}