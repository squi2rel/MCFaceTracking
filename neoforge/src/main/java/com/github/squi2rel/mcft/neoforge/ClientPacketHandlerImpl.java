package com.github.squi2rel.mcft.neoforge;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ClientPacketHandlerImpl {
    public static <P extends CustomPayload> void registerS2C(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        PacketHandlers.registerS2C(id, codec, receiver);
    }

    public static <P extends CustomPayload> void sendC2S(P packet) {
        PacketDistributor.sendToServer(packet);
    }
}
