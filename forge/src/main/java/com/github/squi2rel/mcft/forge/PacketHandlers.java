package com.github.squi2rel.mcft.forge;

import com.github.squi2rel.mcft.network.CustomPacket;
import com.github.squi2rel.mcft.network.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketHandlers {
    private static final String PROTOCOL_VERSION = "1";
    private static int i = 0;
    private static final HashMap<Class<? extends CustomPacket<?>>, Handler<?>> channels = new HashMap<>();

    public static <P extends CustomPacket<P>> Handler<P> register(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec) {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(id, () -> PROTOCOL_VERSION, NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION), NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION));
        Handler<P> handler = new Handler<>(channel);
        channel.registerMessage(i++, clazz, codec.writer(), codec.reader(), (p, c) -> {
            if (c.get().getDirection().getReceptionSide().isServer()) {
                if (handler.serverHandler != null) handler.serverHandler.accept(p, c.get().getSender());
            } else {
                if (handler.clientHandler != null) handler.clientHandler.accept(p);
            }
        });
        return handler;
    }

    @SuppressWarnings("unchecked")
    public static <P extends CustomPacket<P>> void registerC2S(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, BiConsumer<P, ServerPlayerEntity> receiver) {
        channels.computeIfAbsent(clazz, k -> register(clazz, id, codec)).serverHandler = (p, s) -> receiver.accept((P) p, s);
    }

    public static <P extends CustomPacket<P>> void registerS2C(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec) {
        channels.computeIfAbsent(clazz, k -> register(clazz, id, codec));
    }

    public static <P extends CustomPacket<P>> void sendS2C(ServerPlayerEntity player, P packet) {
        channels.get(packet.getClass()).channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @SuppressWarnings("unchecked")
    public static <P extends CustomPacket<P>> void registerS2C(Class<P> clazz, Identifier id, PacketCodec<PacketByteBuf, P> codec, Consumer<P> receiver) {
        Handler<P> handler = (Handler<P>) channels.get(clazz);
        if (handler == null) throw new IllegalStateException("Unregistered clientbound payload " + id);
        handler.clientHandler = receiver;
    }

    public static <P extends CustomPacket<P>> void sendC2S(P packet) {
        channels.get(packet.getClass()).channel.sendToServer(packet);
    }

    public static class Handler<P extends CustomPacket<P>> {
        public final SimpleChannel channel;
        public Consumer<P> clientHandler;
        public BiConsumer<P, ServerPlayerEntity> serverHandler;

        public Handler(SimpleChannel channel) {
            this.channel = channel;
        }
    }
}
