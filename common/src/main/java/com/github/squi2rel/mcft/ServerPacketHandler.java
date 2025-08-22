package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.CustomPacket;
import com.github.squi2rel.mcft.network.PacketCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class ServerPacketHandler {
    @ExpectPlatform
    public static <P extends CustomPacket<P>> void registerC2S(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <P extends CustomPacket<P>> void sendS2C(ServerPlayerEntity player, P packet) {
        throw new AssertionError();
    }
}
