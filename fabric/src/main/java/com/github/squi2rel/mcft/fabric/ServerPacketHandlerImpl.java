package com.github.squi2rel.mcft.fabric;

import com.github.squi2rel.mcft.network.CustomPacket;
import com.github.squi2rel.mcft.network.PacketCodec;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class ServerPacketHandlerImpl {
    public static <P extends CustomPacket<P>> void registerC2S(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buf, responseSender) -> {
            P packet = codec.reader().apply(buf);
            receiver.accept(packet, player);
        });
    }

    public static <P extends CustomPacket<P>> void registerS2C(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec) {
    }

    public static <P extends CustomPacket<P>> void sendS2C(ServerPlayerEntity player, P packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.getCodec().writer().accept(packet, buf);
        ServerPlayNetworking.send(player, packet.getId(), buf);
    }
}
