package com.github.squi2rel.mcft.network;

import com.github.squi2rel.mcft.MCFT;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record TrackingUpdatePayload(UUID player, byte[] data) implements CustomPacket<TrackingUpdatePayload> {
    public static final Identifier ID = Identifier.of(MCFT.MOD_ID, "tracking_update");
    public static final PacketCodec<PacketByteBuf, TrackingUpdatePayload> CODEC = PacketCodec.of((p, buf) -> {
        buf.writeUuid(p.player);
        buf.writeShort(p.data.length);
        buf.writeBytes(p.data);
    }, buf -> {
        UUID uuid = buf.readUuid();
        byte[] bytes = new byte[buf.readShort()];
        buf.readBytes(bytes);
        return new TrackingUpdatePayload(uuid, bytes);
    });

    @Override
    public PacketCodec<PacketByteBuf, TrackingUpdatePayload> getCodec() {
        return CODEC;
    }

    @Override
    public Identifier getId() {
        return ID;
    }
}
