package com.github.squi2rel.mcft.neoforge;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.BiConsumer;

@SuppressWarnings("all")
public class ServerPacketHandlerImpl {
    public static <P extends CustomPayload> void registerC2S(CustomPayload.Id id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        PacketHandlers.registerC2S(id, codec, receiver);
    }

    public static <P extends CustomPayload> void sendS2C(ServerPlayerEntity player, P packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}
