package com.github.squi2rel.mcft;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class ServerPacketHandler {
    @ExpectPlatform
    public static <P extends CustomPayload> void registerC2S(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <P extends CustomPayload> void sendS2C(ServerPlayerEntity player, P packet) {
        throw new AssertionError();
    }
}
