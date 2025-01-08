package ARLib.network;

import ARLib.utils.DimensionUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class PacketEntity implements CustomPacketPayload {



    public static final Type<PacketEntity> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("arlib", "packetentity"));


    public PacketEntity(CompoundTag entityTag, CompoundTag tag) {
        this.tag = tag;
        this.entityTag = entityTag;
    }

    CompoundTag entityTag;
    CompoundTag tag;

    public CompoundTag getTag() {
        return tag;
    }
    public CompoundTag entityTag(){return entityTag;}

    public static final StreamCodec<ByteBuf, PacketEntity> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            PacketEntity::entityTag,
            ByteBufCodecs.COMPOUND_TAG,
            PacketEntity::getTag,
            PacketEntity::new
    );

    @OnlyIn(Dist.CLIENT)     // <- this is bc the server shits itself on this: Minecraft.getInstance().level;
    public static void readClient_onlyonclient(final PacketEntity data, final IPayloadContext context) {
        // use the current Dimension, the client does not need to find the dimension by String
        ClientLevel world = Minecraft.getInstance().level;
        for (int i = 0; i < world.getEntityCount(); i++) {
            Entity e = world.getEntity(i);
            if (e instanceof INetworkTagReceiver ntr) {
                if (e.getUUID().equals(data.entityTag.getUUID("uuid"))) {
                    ntr.readClient(data.tag);
                }
            }
        }
    }
    //  this can not be dist.client because it is used in register method
    public static void readClient(final PacketEntity data, final IPayloadContext context) {
        readClient_onlyonclient(data,context);
    }
    public static void readServer(final PacketEntity data, final IPayloadContext context) {
        Level world = DimensionUtils.getDimensionLevelServer(data.entityTag.getString("level"));
        if (world instanceof ServerLevel l) {
            Entity e = l.getEntities().get(data.entityTag.getUUID("uuid"));
            if (e instanceof INetworkTagReceiver) {
                ((INetworkTagReceiver) e).readServer(data.getTag());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                PacketEntity.TYPE,
                PacketEntity.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        PacketEntity::readClient,
                        PacketEntity::readServer
                )
        );
    }


    public static PacketEntity getEntityPacket(Entity e, CompoundTag tag){
        return getEntityPacket(e.getUUID(), DimensionUtils.getLevelId(e.level()) , tag);
    }
    public static PacketEntity getEntityPacket(UUID entity,String levelId, CompoundTag tag){
        CompoundTag uuidTag = new CompoundTag();
        uuidTag.putUUID("uuid",entity);
        uuidTag.putString("level", levelId);
        return new PacketEntity(
                uuidTag,
                tag
        );
    }

}