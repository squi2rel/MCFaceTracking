package com.github.squi2rel.mcft.network;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.tracking.EyeTrackingRect;
import com.github.squi2rel.mcft.tracking.MouthTrackingRect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record TrackingParamsPayload(UUID player, EyeTrackingRect eyeR, EyeTrackingRect eyeL, MouthTrackingRect mouth, boolean flat) implements CustomPacket<TrackingParamsPayload> {
    public static final Identifier ID = Identifier.of(MCFT.MOD_ID, "tracking_params");
    public static final PacketCodec<PacketByteBuf, TrackingParamsPayload> CODEC = PacketCodec.of((p, buf) -> {
        buf.writeUuid(p.player);
        p.eyeR.write(buf);
        p.eyeL.write(buf);
        p.mouth.write(buf);
        buf.writeBoolean(p.flat);
    }, buf -> new TrackingParamsPayload(buf.readUuid(), EyeTrackingRect.read(buf), EyeTrackingRect.read(buf), MouthTrackingRect.read(buf), buf.readBoolean()));

    @Override
    public PacketCodec<PacketByteBuf, TrackingParamsPayload> getCodec() {
        return CODEC;
    }

    @Override
    public Identifier getId() {
        return ID;
    }
}
