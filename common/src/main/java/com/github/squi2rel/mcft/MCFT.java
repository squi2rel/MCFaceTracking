package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.ConfigPayload;
import com.github.squi2rel.mcft.network.TrackingParamsPayload;
import com.github.squi2rel.mcft.network.TrackingUpdatePayload;
import com.google.gson.Gson;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MCFT {
    public static final String MOD_ID = "mcft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ConcurrentHashMap<UUID, FTModel> models = new ConcurrentHashMap<>();

    public static String version = Platform.getVersion();
    public static Path configPath = Platform.getConfigPath();
    public static ServerConfig config;

    public static void onInitialize() {
        config = loadConfig(ServerConfig.class, configPath);

        ServerPacketHandler.registerC2S(TrackingParamsPayload.class, TrackingParamsPayload.ID, TrackingParamsPayload.CODEC, (payload, p) -> {
            FTModel old = models.get(p.getUuid());
            if (old == null) LOGGER.info("玩家 {} 正在使用MCFT", Objects.requireNonNull(p.getDisplayName()).getString());
            FTModel model = new FTModel(payload.eyeR(), payload.eyeL(), payload.mouth(), payload.flat());
            model.validate(true);
            if (old != null) model.enabled = old.enabled;
            models.put(p.getUuid(), model);
            if (model.enabled) {
                TrackingParamsPayload packet = new TrackingParamsPayload(p.getUuid(), model.eyeR, model.eyeL, model.mouth, model.isFlat);
                for (ServerPlayerEntity player : Objects.requireNonNull(p.getServer()).getPlayerManager().getPlayerList()) ServerPacketHandler.sendS2C(player, packet);
            }
        });

        ServerPacketHandler.registerC2S(TrackingUpdatePayload.class, TrackingUpdatePayload.ID, TrackingUpdatePayload.CODEC, (payload, p) -> {
            FTModel model = models.get(p.getUuid());
            if (model == null || System.currentTimeMillis() - model.lastReceived + 10 < 1000 / config.fps) return;
            model.readSync(payload.data());
            model.validate(false);
            if (!model.enabled) {
                model.enabled = true;
                LOGGER.info("玩家 {} 已连接OSC", Objects.requireNonNull(p.getDisplayName()).getString());
                TrackingParamsPayload packet = new TrackingParamsPayload(p.getUuid(), model.eyeR, model.eyeL, model.mouth, model.isFlat);
                for (ServerPlayerEntity player : Objects.requireNonNull(p.getServer()).getPlayerManager().getPlayerList()) ServerPacketHandler.sendS2C(player, packet);
            }
            TrackingUpdatePayload packet = new TrackingUpdatePayload(p.getUuid(), payload.data());
            for (ServerPlayerEntity player : p.getServerWorld().getPlayers(player ->
                    player.getPos().isInRange(p.getPos(), config.syncRadius)
            )) ServerPacketHandler.sendS2C(player, packet);
        });

        Platform.register();
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        models.forEach((u, m) -> {
            if (m.enabled) ServerPacketHandler.sendS2C(player, new TrackingParamsPayload(u, m.eyeR, m.eyeL, m.mouth, m.isFlat));
        });
        ServerPacketHandler.sendS2C(player, new ConfigPayload(version, config.fps));
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        models.remove(player.getUuid());
    }

    public static <T> T loadConfig(Class<T> clazz, Path path) {
        try {
            return new Gson().fromJson(Files.readString(path), clazz);
        } catch (Exception e) {
            try {
                saveConfig(clazz.getDeclaredConstructor().newInstance(), path);
                return new Gson().fromJson(Files.readString(path), clazz);
            } catch (Exception ex) {
                RuntimeException th = new RuntimeException("Failed to load config file", ex);
                th.addSuppressed(e);
                throw th;
            }
        }
    }

    public static void saveConfig(Object config, Path path) {
        try {
            Files.writeString(path, new Gson().toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}