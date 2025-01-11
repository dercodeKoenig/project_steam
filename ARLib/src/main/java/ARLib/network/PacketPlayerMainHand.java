package ARLib.network;

import ARLib.utils.DimensionUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class PacketPlayerMainHand implements CustomPacketPayload {



    public static final Type<PacketPlayerMainHand> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("arlib", "packetplayermainhand"));


    public PacketPlayerMainHand(CompoundTag tag) {
        this.tag = tag;
    }

    CompoundTag tag;

    public CompoundTag getTag() {
        return tag;
    }


    public static final StreamCodec<ByteBuf, PacketPlayerMainHand> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            PacketPlayerMainHand::getTag,
            PacketPlayerMainHand::new
    );

    @OnlyIn(Dist.CLIENT)     // <- this is bc the server shits itself on this: Minecraft.getInstance().level;
    public static void readClient_onlyonclient(final PacketPlayerMainHand data, final IPayloadContext context) {
        ItemStack selectedStack = Minecraft.getInstance().player.getInventory().getSelected();
        if (selectedStack.getItem() instanceof INetworkTagReceiver r)
            r.readClient(data.getTag());
    }
    //  this can not be dist.client because it is used in register method
    public static void readClient(final PacketPlayerMainHand data, final IPayloadContext context) {
        readClient_onlyonclient(data,context);
    }
    public static void readServer(final PacketPlayerMainHand data, final IPayloadContext context) {
        ItemStack selectedStack = context.player().getInventory().getSelected();
        if(selectedStack.getItem() instanceof INetworkTagReceiver r && context.player() instanceof ServerPlayer p)
            r.readServer(data.getTag(), p);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                PacketPlayerMainHand.TYPE,
                PacketPlayerMainHand.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        PacketPlayerMainHand::readClient,
                        PacketPlayerMainHand::readServer
                )
        );
    }


}