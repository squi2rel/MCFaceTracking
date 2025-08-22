package com.github.squi2rel.mcft.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface CustomPacket<T> {
    PacketCodec<PacketByteBuf, T> getCodec();
    Identifier getId();
}
