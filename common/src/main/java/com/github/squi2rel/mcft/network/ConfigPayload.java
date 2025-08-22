package com.github.squi2rel.mcft.network;

import com.github.squi2rel.mcft.MCFT;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ConfigPayload(String version, int fps) implements CustomPacket<ConfigPayload> {
    public static final Identifier ID = Identifier.of(MCFT.MOD_ID, "config");
    public static final PacketCodec<PacketByteBuf, ConfigPayload> CODEC = PacketCodec.of((p, buf) -> {
        buf.writeString(p.version, 16);
        buf.writeInt(p.fps);
    }, buf -> new ConfigPayload(buf.readString(16), buf.readInt()));

    @Override
    public PacketCodec<PacketByteBuf, ConfigPayload> getCodec() {
        return CODEC;
    }

    @Override
    public Identifier getId() {
        return ID;
    }
}
