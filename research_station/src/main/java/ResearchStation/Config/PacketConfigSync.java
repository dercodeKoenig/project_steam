package ResearchStation.Config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


public  class PacketConfigSync implements CustomPacketPayload {

    public static final Type<PacketConfigSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("research_station", "packet_config_sync"));


    public PacketConfigSync(String config) {
        this.config = config;
    }

    String config;
    public String getConfig() {
        return config;
    }


    public static final StreamCodec<ByteBuf, PacketConfigSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PacketConfigSync::getConfig,
            PacketConfigSync::new
    );

    public static void readClient(final PacketConfigSync data, final IPayloadContext context) {
        String config = data.getConfig();
        Config.INSTANCE.loadConfig(config);
    }
    public static void readServer(final PacketConfigSync data, final IPayloadContext context) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                PacketConfigSync.TYPE,
                PacketConfigSync.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        PacketConfigSync::readClient,
                        PacketConfigSync::readServer
                )
        );
    }
}
