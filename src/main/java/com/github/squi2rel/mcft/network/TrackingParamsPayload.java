package com.github.squi2rel.mcft.network;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.tracking.EyeTrackingRect;
import com.github.squi2rel.mcft.tracking.MouthTrackingRect;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record TrackingParamsPayload(UUID player, EyeTrackingRect eyeR, EyeTrackingRect eyeL, MouthTrackingRect mouth, boolean flat) implements CustomPayload {
    public static final Identifier TRACKING_PARAMS_PAYLOAD_ID = Identifier.of(MCFT.MOD_ID, "tracking_params");
    public static final CustomPayload.Id<TrackingParamsPayload> ID = new CustomPayload.Id<>(TRACKING_PARAMS_PAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, TrackingParamsPayload> CODEC = PacketCodec.of((p, buf) -> {
        buf.writeUuid(p.player);
        p.eyeR.write(buf);
        p.eyeL.write(buf);
        p.mouth.write(buf);
        buf.writeBoolean(p.flat);
    }, buf -> new TrackingParamsPayload(buf.readUuid(), EyeTrackingRect.read(buf), EyeTrackingRect.read(buf), MouthTrackingRect.read(buf), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
    }
}
