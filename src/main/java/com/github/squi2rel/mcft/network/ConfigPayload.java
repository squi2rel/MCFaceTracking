package com.github.squi2rel.mcft.network;

import com.github.squi2rel.mcft.MCFT;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigPayload(int fps) implements CustomPayload {
    public static final Identifier CONFIG_PAYLOAD_ID = Identifier.of(MCFT.MOD_ID, "config");
    public static final CustomPayload.Id<ConfigPayload> ID = new CustomPayload.Id<>(CONFIG_PAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, ConfigPayload> CODEC = PacketCodec.of((p, buf) -> buf.writeInt(p.fps), buf -> new ConfigPayload(buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}
