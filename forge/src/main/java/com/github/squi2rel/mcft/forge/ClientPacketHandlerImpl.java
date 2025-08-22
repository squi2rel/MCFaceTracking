package com.github.squi2rel.mcft.forge;

import com.github.squi2rel.mcft.network.CustomPacket;
import com.github.squi2rel.mcft.network.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ClientPacketHandlerImpl {
    public static <P extends CustomPacket<P>> void registerS2C(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        PacketHandlers.registerS2C(clazz, id, codec, receiver);
    }

    public static <P extends CustomPacket<P>> void sendC2S(P packet) {
        PacketHandlers.sendC2S(packet);
    }
}
