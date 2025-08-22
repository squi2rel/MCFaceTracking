package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.CustomPacket;
import com.github.squi2rel.mcft.network.PacketCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ClientPacketHandler {
    @ExpectPlatform
    public static <P extends CustomPacket<P>> void registerS2C(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <P extends CustomPacket<P>> void sendC2S(P packet) {
        throw new AssertionError();
    }
}
