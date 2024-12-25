package ARLib.gui;

import ARLib.gui.modules.GuiModuleBase;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.print.attribute.standard.Sides;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IGuiHandler {


    CustomPacketPayload getNetworkPacketForTag_client(CompoundTag tag);
    CustomPacketPayload getNetworkPacketForTag_server(CompoundTag tag);

    void onGuiClientTick();

    void registerModule(GuiModuleBase guiModule);

    List<GuiModuleBase> getModules();

    Map<UUID, Integer> getPlayersTrackingGui();

    @OnlyIn(Dist.CLIENT)
    default void openGui() {
        openGui(176, 166);
    }
    @OnlyIn(Dist.CLIENT)
    default void openGui(int w, int h) {
        sendPing();
        // fix for not syncing in creative mode
        Minecraft.getInstance().player.inventoryMenu.setCarried(ItemStack.EMPTY);
        Minecraft.getInstance().setScreen(new ModularScreen(this, w, h));
    }

    @OnlyIn(Dist.CLIENT)
    default void onGuiClose() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("closeGui", Minecraft.getInstance().player.getUUID());
        sendToServer(tag);
    }

    @OnlyIn(Dist.CLIENT)
    default void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(getNetworkPacketForTag_client(tag));
    }

    default void readClient(CompoundTag tag) {
        for (GuiModuleBase m : getModules()) {
            m.client_handleDataSyncedToClient(tag);
        }
    }

    default void sendToTrackingClients(CompoundTag tag) {
        // this can be called on client side, but it does nothing because the list us empty
        // so it will not cause a crash when server is null
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (UUID uid : getPlayersTrackingGui().keySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(uid);
            if(p!=null) {
                PacketDistributor.sendToPlayer(p, getNetworkPacketForTag_server(tag));
            }
        }
    }

    default void serverTick() {
        if (!getPlayersTrackingGui().isEmpty()) {
            for (GuiModuleBase m : getModules()) {
                m.serverTick();
            }
            // if a player has not sent a gui ping for 10 seconds, he no longer has the gui open
            // this should usually not happen because the client will unregister itself on gui close but just to be safe....
            for (UUID uid : getPlayersTrackingGui().keySet()) {
                getPlayersTrackingGui().put(uid, getPlayersTrackingGui().get(uid) + 1);
                if (getPlayersTrackingGui().get(uid) > 200) {
                    removePlayerFromGui(uid);
                }
            }
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

    default void removePlayerFromGui(UUID uid) {
        getPlayersTrackingGui().remove(uid);
        Player p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uid);
        if (p != null) {
            dropPlayersCarriedItem(p);
        }
    }


    default void readServer(CompoundTag tag) {
        if (tag.contains("guiPing")) {
            UUID uid = tag.getUUID("guiPing");
            // update data asap when a client opens the gui new
            if (!getPlayersTrackingGui().containsKey(uid)) {
                CompoundTag guiData = new CompoundTag();
                for (GuiModuleBase guiModule : getModules()) {
                    guiModule.server_writeDataToSyncToClient(guiData);
                }
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                ServerPlayer p = server.getPlayerList().getPlayer(uid);
                if(p!=null){
                PacketDistributor.sendToPlayer(p, getNetworkPacketForTag_server(guiData));
            }
            }
            getPlayersTrackingGui().put(uid, 0);
        }
        if (tag.contains("closeGui")) {
            UUID uid = tag.getUUID("closeGui");
            // a client said he no longer has the gui open
            if (getPlayersTrackingGui().containsKey(uid)) {
                removePlayerFromGui(uid);
            }
        }
        for (GuiModuleBase m : getModules()) {
            m.server_readNetworkData(tag);
        }
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
    }

    @OnlyIn(Dist.CLIENT)
    default void sendPing() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("guiPing", Minecraft.getInstance().player.getUUID());
        sendToServer(tag);
    }

}