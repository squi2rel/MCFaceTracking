package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.ConfigPayload;
import com.github.squi2rel.mcft.network.TrackingParamsPayload;
import com.github.squi2rel.mcft.network.TrackingUpdatePayload;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class MCFT implements ModInitializer {
    public static final String MOD_ID = "mcft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static HashMap<UUID, FTModel> models = new HashMap<>();

    public static final String version = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().toString();
    public static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mcft-server.json");
    public static ServerConfig config;

    @Override
    public void onInitialize() {
        config = loadConfig(ServerConfig.class, configPath);

        TrackingParamsPayload.register();
        TrackingUpdatePayload.register();
        ConfigPayload.register();

        ServerPlayNetworking.registerGlobalReceiver(TrackingParamsPayload.ID, (payload, context) -> {
            ServerPlayerEntity p = context.player();
            FTModel old = models.get(p.getUuid());
            if (old == null) LOGGER.info("玩家 {} 正在使用MCFT", Objects.requireNonNull(p.getDisplayName()).getString());
            FTModel model = new FTModel(payload.eyeR(), payload.eyeL(), payload.mouth(), payload.flat());
            model.validate(true);
            if (old != null) model.enabled = old.enabled;
            models.put(p.getUuid(), model);
            if (model.enabled) {
                TrackingParamsPayload packet = new TrackingParamsPayload(p.getUuid(), model.eyeR, model.eyeL, model.mouth, model.isFlat);
                for (ServerPlayerEntity player : PlayerLookup.all(Objects.requireNonNull(p.getServer()))) ServerPlayNetworking.send(player, packet);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(TrackingUpdatePayload.ID, (payload, context) -> {
            ServerPlayerEntity p = context.player();
            FTModel model = models.get(p.getUuid());
            if (model == null || System.currentTimeMillis() - model.lastReceived - 10 < 1000 / config.fps) return;
            model.readSync(payload.data());
            model.validate(false);
            if (!model.enabled) {
                model.enabled = true;
                LOGGER.info("玩家 {} 已连接OSC", Objects.requireNonNull(p.getDisplayName()).getString());
                TrackingParamsPayload packet = new TrackingParamsPayload(p.getUuid(), model.eyeR, model.eyeL, model.mouth, model.isFlat);
                for (ServerPlayerEntity player : PlayerLookup.all(Objects.requireNonNull(p.getServer()))) ServerPlayNetworking.send(player, packet);
            }
            TrackingUpdatePayload packet = new TrackingUpdatePayload(p.getUuid(), payload.data());
            for (ServerPlayerEntity player : PlayerLookup.around(p.getServerWorld(), p.getPos(), config.syncRadius)) ServerPlayNetworking.send(player, packet);
        });

        ServerPlayConnectionEvents.JOIN.register((h, s, c) -> {
            models.forEach((u, m) -> {
                if (m.enabled) ServerPlayNetworking.send(h.getPlayer(), new TrackingParamsPayload(u, m.eyeR, m.eyeL, m.mouth, m.isFlat));
            });
            ServerPlayNetworking.send(h.getPlayer(), new ConfigPayload(version, config.fps));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> models.remove(h.getPlayer().getUuid()));
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