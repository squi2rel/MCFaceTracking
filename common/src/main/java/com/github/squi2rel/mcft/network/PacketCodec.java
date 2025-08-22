package com.github.squi2rel.mcft.network;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record PacketCodec<T, P>(BiConsumer<P, T> writer, Function<T, P> reader) {
    public static <T, P> PacketCodec<T, P> of(BiConsumer<P, T> writer, Function<T, P> reader) {
        return new PacketCodec<>(writer, reader);
    }
}
