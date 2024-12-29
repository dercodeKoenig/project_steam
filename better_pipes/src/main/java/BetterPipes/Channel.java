package BetterPipes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Channel {

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("betterpipes", "packet_flow_update"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(PacketFlowUpdate.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketFlowUpdate::read)
                .encoder(PacketFlowUpdate::write)
                .consumerMainThread(PacketFlowUpdate::handle)
                .add();

        net.messageBuilder(PacketFluidAmountUpdate.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketFluidAmountUpdate::read)
                .encoder(PacketFluidAmountUpdate::write)
                .consumerMainThread(PacketFluidAmountUpdate::handle)
                .add();

        net.messageBuilder(PacketFluidUpdate.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketFluidUpdate::read)
                .encoder(PacketFluidUpdate::write)
                .consumerMainThread(PacketFluidUpdate::handle)
                .add();

        net.messageBuilder(PacketRequestInitialData.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketRequestInitialData::read)
                .encoder(PacketRequestInitialData::write)
                .consumerMainThread(PacketRequestInitialData::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }


    public static <MSG> void sendToPlayersTrackingBE(MSG message, BlockEntity be) {
        if (be == null || be.getLevel() == null || be.getLevel().isClientSide()) {
            return; // Ensure we're on the server and the BlockEntity is valid.
        }

        // Get the world and position of the BlockEntity
        ServerLevel level = (ServerLevel) be.getLevel();
        ChunkPos chunkPos = new ChunkPos(be.getBlockPos());

        // Use the chunk map to get players tracking the chunk
        ChunkMap chunkMap = level.getChunkSource().chunkMap;

        chunkMap.getPlayers(chunkPos, false).forEach(player -> {
            // Send the packet to the player using PacketDistributor
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
        });
    }
}
