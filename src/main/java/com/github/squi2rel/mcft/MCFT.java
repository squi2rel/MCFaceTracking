package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.TrackingParamsPayload;
import com.github.squi2rel.mcft.network.TrackingUpdatePayload;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

	@Override
	public void onInitialize() {
		TrackingParamsPayload.register();
		TrackingUpdatePayload.register();
		ServerPlayNetworking.registerGlobalReceiver(TrackingParamsPayload.ID, (payload, context) -> {
			ServerPlayerEntity p = context.player();
			if (models.get(p.getUuid()) == null) LOGGER.info("玩家 {} 正在使用MCFT", Objects.requireNonNull(p.getDisplayName()).getString());
			models.put(p.getUuid(), new FTModel(payload.eyeR(), payload.eyeL(), payload.mouth()));
		});
		ServerPlayNetworking.registerGlobalReceiver(TrackingUpdatePayload.ID, (payload, context) -> {
			ServerPlayerEntity p = context.player();
			FTModel model = models.get(p.getUuid());
			if (model == null) return;
			model.readSync(payload.data());
			if (!model.enabled) {
				model.enabled = true;
				LOGGER.info("玩家 {} 已连接OSC", Objects.requireNonNull(p.getDisplayName()).getString());
				TrackingParamsPayload packet = new TrackingParamsPayload(p.getUuid(), model.eyeR, model.eyeL, model.mouth);
				for (ServerPlayerEntity player : PlayerLookup.all(Objects.requireNonNull(p.getServer()))) {
					if (player.equals(p)) continue;
					ServerPlayNetworking.send(player, packet);
				}
			}
			TrackingUpdatePayload packet = new TrackingUpdatePayload(p.getUuid(), payload.data());
			for (ServerPlayerEntity player : PlayerLookup.around(p.getServerWorld(), p.getPos(), 128)) {
				if (player.equals(p)) continue;
				ServerPlayNetworking.send(player, packet);
			}
		});
		ServerPlayConnectionEvents.JOIN.register((h, s, c) -> models.forEach((u, m) -> {
			if (m.enabled) ServerPlayNetworking.send(h.getPlayer(), new TrackingParamsPayload(u, m.eyeR, m.eyeL, m.mouth));
		}));
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

	public static void saveConfig(Object config, Path path) throws IOException {
		Files.writeString(path, new Gson().toJson(config));
	}
}