package com.github.squi2rel.mcft.network;

import com.github.squi2rel.mcft.MCFT;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record TrackingUpdatePayload(UUID player, byte[] data) implements CustomPayload {
    public static final Identifier TRACKING_UPDATE_PAYLOAD_ID = Identifier.of(MCFT.MOD_ID, "tracking_update");
    public static final CustomPayload.Id<TrackingUpdatePayload> ID = new CustomPayload.Id<>(TRACKING_UPDATE_PAYLOAD_ID);
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
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
    }
}
