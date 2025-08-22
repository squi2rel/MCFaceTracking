package com.github.squi2rel.mcft.network;

import com.github.squi2rel.mcft.MCFT;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigPayload(String version, int fps) implements CustomPayload {
    public static final Identifier CONFIG_PAYLOAD_ID = Identifier.of(MCFT.MOD_ID, "config");
    public static final Id<ConfigPayload> ID = new Id<>(CONFIG_PAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, ConfigPayload> CODEC = PacketCodec.of((p, buf) -> {
        buf.writeString(p.version, 16);
        buf.writeInt(p.fps);
    }, buf -> new ConfigPayload(buf.readString(16), buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
