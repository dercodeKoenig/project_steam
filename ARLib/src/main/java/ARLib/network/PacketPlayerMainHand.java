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

    public PacketPlayerMainHand(UUID id, CompoundTag tag) {
        this.tag = tag;
        this.UUIDtag = new CompoundTag();
        UUIDtag.putUUID("uuid", id);
    }

    public PacketPlayerMainHand(CompoundTag UUIDtag, CompoundTag tag) {
        this.tag = tag;
        this.UUIDtag = UUIDtag;
    }

    CompoundTag UUIDtag;
    CompoundTag tag;

    public CompoundTag getTag() {
        return tag;
    }
    public CompoundTag getId(){return UUIDtag;}


    public static final StreamCodec<ByteBuf, PacketPlayerMainHand> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            PacketPlayerMainHand::getId,
            ByteBufCodecs.COMPOUND_TAG,
            PacketPlayerMainHand::getTag,
            PacketPlayerMainHand::new
    );

    @OnlyIn(Dist.CLIENT)     // <- this is bc the server shits itself on this: Minecraft.getInstance().level;
    public static void readClient_onlyonclient(final PacketPlayerMainHand data, final IPayloadContext context) {
        ItemStack selectedStack =        Minecraft.getInstance().player.getInventory().getSelected();
        if(selectedStack.getItem() instanceof INetworkItemStackTagReceiver r)
            r.readClient(data.getTag());
    }
    //  this can not be dist.client because it is used in register method
    public static void readClient(final PacketPlayerMainHand data, final IPayloadContext context) {
        readClient_onlyonclient(data,context);
    }
    public static void readServer(final PacketPlayerMainHand data, final IPayloadContext context) {
        UUID id = data.getId().getUUID("uuid");
        Player p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(id);
        ItemStack selectedStack = p.getInventory().getSelected();
        if(selectedStack.getItem() instanceof INetworkItemStackTagReceiver r)
            r.readServer(data.getTag(), selectedStack);
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