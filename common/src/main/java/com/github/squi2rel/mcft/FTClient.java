package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.TrackingParamsPayload;
import com.github.squi2rel.mcft.network.TrackingUpdatePayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import net.minecraft.client.MinecraftClient;

import java.util.Objects;

public class FTClient {
    public static void uploadParams(FTModel model) {
        if (!MCFTClient.connected) return;
        ClientPacketHandler.sendC2S(new TrackingParamsPayload(Objects.requireNonNull(MinecraftClient.getInstance().player).getUuid(), model.eyeR, model.eyeL, model.mouth, model.isFlat));
    }

    public static void writeSync(FTModel model) {
        if (!MCFTClient.connected || !model.active()) return;
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer();
        try {
            model.eyeR.writeSync(buf);
            model.eyeL.writeSync(buf);
            model.mouth.writeSync(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            ClientPacketHandler.sendC2S(new TrackingUpdatePayload(Objects.requireNonNull(MinecraftClient.getInstance().player).getUuid(), data));
        } finally {
            buf.release();
        }
    }
}
