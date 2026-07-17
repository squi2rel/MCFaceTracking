package com.github.squi2rel.mcft.fabric;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class ServerPacketHandlerImpl {
    public static <P extends CustomPayload> void registerC2S(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        PayloadTypeRegistry.playC2S().register(id, codec);
        ServerPlayNetworking.registerGlobalReceiver(id, (packet, context) -> receiver.accept(packet, context.player()));
    }

    public static <P extends CustomPayload> void registerS2C(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec) {
        PayloadTypeRegistry.playS2C().register(id, codec);
    }

    public static <P extends CustomPayload> void sendS2C(ServerPlayerEntity player, P packet) {
        ServerPlayNetworking.send(player, packet);
    }
}
