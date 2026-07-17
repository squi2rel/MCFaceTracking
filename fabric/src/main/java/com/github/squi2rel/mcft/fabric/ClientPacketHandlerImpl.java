package com.github.squi2rel.mcft.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ClientPacketHandlerImpl {
    public static <P extends CustomPayload> void registerS2C(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        ClientPlayNetworking.registerGlobalReceiver(id, (packet, context) -> receiver.accept(packet));
    }

    public static <P extends CustomPayload> void sendC2S(P packet) {
        ClientPlayNetworking.send(packet);
    }
}
