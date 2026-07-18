package com.github.squi2rel.mcft.forge;

import com.github.squi2rel.mcft.network.CustomPacket;
import com.github.squi2rel.mcft.network.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

@SuppressWarnings("all")
public class ServerPacketHandlerImpl {
    public static <P extends CustomPacket<P>> void registerC2S(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        PacketHandlers.registerC2S(clazz, id, codec, receiver);
    }

    public static <P extends CustomPacket<P>> void registerS2C(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec) {
        PacketHandlers.registerS2C(clazz, id, codec);
    }

    public static <P extends CustomPacket<P>> void sendS2C(ServerPlayerEntity player, P packet) {
        PacketHandlers.sendS2C(player, packet);
    }
}
