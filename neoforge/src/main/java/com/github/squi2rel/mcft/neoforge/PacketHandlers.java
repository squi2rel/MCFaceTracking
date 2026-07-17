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
        Handler<P> handler = (Handler<P>) channels.computeIfAbsent(id, k -> new Handler<>(codec));
        handler.serverbound = true;
        handler.serverHandler = receiver;
    }

    @SuppressWarnings("unchecked")
    public static <P extends CustomPayload> void registerS2C(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec) {
        Handler<P> handler = (Handler<P>) channels.computeIfAbsent(id, k -> new Handler<>(codec));
        handler.clientbound = true;
    }

    @SuppressWarnings("unchecked")
    public static <P extends CustomPayload> void registerS2C(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        Handler<P> handler = (Handler<P>) channels.get(id);
        if (handler == null || !handler.clientbound) throw new IllegalStateException("Unregistered clientbound payload " + id);
        handler.clientHandler = receiver;
    }

    @SuppressWarnings("unchecked")
    public static <P extends CustomPayload> void register(PayloadRegistrar registrar) {
        for (Map.Entry<CustomPayload.Id<? extends CustomPayload>, Handler<? extends CustomPayload>> entry : channels.entrySet()) {
            CustomPayload.Id<P> id = (CustomPayload.Id<P>) entry.getKey();
            Handler<P> handler = (Handler<P>) entry.getValue();
            if (handler.clientbound && handler.serverbound) {
                registrar.playBidirectional(
                        id, handler.packetCodec,
                        new DirectionalPayloadHandler<>(
                                (p, c) -> handler.handleClientbound(p),
                                (p, c) -> handler.handleServerbound(p, (ServerPlayerEntity) c.player())
                        )
                );
            } else {
                if (handler.clientbound) {
                    registrar.playToClient(id, handler.packetCodec, (p, c) -> handler.handleClientbound(p));
                } else if (handler.serverbound) {
                    registrar.playToServer(id, handler.packetCodec, (p, c) -> handler.handleServerbound(p, (ServerPlayerEntity) c.player()));
                }
            }
        }
    }

    public static class Handler<P extends CustomPayload> {
        public final PacketCodec<PacketByteBuf, P> packetCodec;
        public boolean clientbound;
        public boolean serverbound;
        public volatile Consumer<P> clientHandler;
        public volatile BiConsumer<P, ServerPlayerEntity> serverHandler;

        public Handler(PacketCodec<PacketByteBuf, P> packetCodec) {
            this.packetCodec = packetCodec;
        }

        public void handleClientbound(P packet) {
            Consumer<P> receiver = clientHandler;
            if (receiver != null) receiver.accept(packet);
        }

        public void handleServerbound(P packet, ServerPlayerEntity player) {
            BiConsumer<P, ServerPlayerEntity> receiver = serverHandler;
            if (receiver != null) receiver.accept(packet, player);
        }
    }
}
