package com.github.squi2rel.mcft.neoforge;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class PacketHandlers {
    private static final HashMap<CustomPayload.Id<? extends CustomPayload>, Handler<? extends CustomPayload>> channels = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <P extends CustomPayload> void registerC2S(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        channels.computeIfAbsent(id, k -> new Handler<>(codec)).serverHandler = (p, s) -> receiver.accept((P) p, s);
    }

    @SuppressWarnings("unchecked")
    public static <P extends CustomPayload> void registerS2C(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        channels.computeIfAbsent(id, k -> new Handler<>(codec)).clientHandler = p -> receiver.accept((P) p);
    }

    @SuppressWarnings("unchecked")
    public static <P extends CustomPayload> void register(PayloadRegistrar registrar) {
        for (Map.Entry<CustomPayload.Id<? extends CustomPayload>, Handler<? extends CustomPayload>> entry : channels.entrySet()) {
            CustomPayload.Id<P> id = (CustomPayload.Id<P>) entry.getKey();
            Handler<P> handler = (Handler<P>) entry.getValue();
            if (handler.clientHandler != null && handler.serverHandler != null) {
                registrar.playBidirectional(
                        id, handler.packetCodec,
                        new DirectionalPayloadHandler<>(
                                (p, c) -> handler.clientHandler.accept(p),
                                (p, c) -> handler.serverHandler.accept(p, (ServerPlayerEntity) c.player())
                        )
                );
            } else {
                if (handler.clientHandler != null) {
                    registrar.playToClient(id, handler.packetCodec, (p, c) -> handler.clientHandler.accept(p));
                } else if (handler.serverHandler != null) {
                    registrar.playToServer(id, handler.packetCodec, (p, c) -> handler.serverHandler.accept(p, (ServerPlayerEntity) c.player()));
                }
            }
        }
    }

    public static class Handler<P extends CustomPayload> {
        public final PacketCodec<PacketByteBuf, P> packetCodec;
        public Consumer<P> clientHandler;
        public BiConsumer<P, ServerPlayerEntity> serverHandler;

        public Handler(PacketCodec<PacketByteBuf, P> packetCodec) {
            this.packetCodec = packetCodec;
        }
    }
}
