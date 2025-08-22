package com.github.squi2rel.mcft.fabric;

import com.github.squi2rel.mcft.network.CustomPacket;
import com.github.squi2rel.mcft.network.PacketCodec;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ClientPacketHandlerImpl {
    public static <P extends CustomPacket<P>> void registerS2C(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, responseSender) -> {
            P packet = codec.reader().apply(buf);
            receiver.accept(packet);
        });
    }

    public static <P extends CustomPacket<P>> void sendC2S(P packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.getCodec().writer().accept(packet, buf);
        ClientPlayNetworking.send(packet.getId(), buf);
    }
}
