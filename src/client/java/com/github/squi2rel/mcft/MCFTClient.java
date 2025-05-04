package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.TrackingParamsPayload;
import com.github.squi2rel.mcft.network.TrackingUpdatePayload;
import com.github.squi2rel.mcft.services.DNS;
import com.github.squi2rel.mcft.services.HTTP;
import com.github.squi2rel.mcft.services.OSC;
import com.github.squi2rel.mcft.ui.UVGridScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static com.github.squi2rel.mcft.FTModel.model;

@SuppressWarnings("resource")
public class MCFTClient implements ClientModInitializer {
    private static long lastSync = System.currentTimeMillis();
    private static final int fps = 30;
    private static boolean configScreen = false;
    public static HashMap<UUID, FTModel> uuidToModel = new HashMap<>();
    public static Config config;
    public static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mcft.json");

	@Override
	public void onInitializeClient() {
        config = MCFT.loadConfig(Config.class, configPath);
        model = config.model;
        try {
            HTTP.init();
            OSC.init();
            DNS.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ClientPlayNetworking.registerGlobalReceiver(TrackingParamsPayload.ID, (p, context) -> context.client().execute(() -> uuidToModel.put(p.player(), new FTModel(p.eyeR(), p.eyeL(), p.mouth()))));
        ClientPlayNetworking.registerGlobalReceiver(TrackingUpdatePayload.ID, (p, context) -> context.client().execute(() -> {
            FTModel model = uuidToModel.get(p.player());
            if (model == null) return;
            model.readSync(p.data());
        }));

        WorldRenderEvents.LAST.register(e -> {
            if (model.active() && System.currentTimeMillis() - lastSync > 1000 / fps) {
                FTClient.writeSync(model);
                lastSync = System.currentTimeMillis();
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((d, r) -> d.register(ClientCommandManager.literal("mcft").executes(s -> {
            configScreen = true;
            return 1;
        })));

        ClientPlayConnectionEvents.JOIN.register((h, s, c) -> FTClient.uploadParams(model));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            model.lastReceived = OSC.lastReceived;
            uuidToModel.put(client.player.getUuid(), model);
            uuidToModel.entrySet().removeIf(entry -> Objects.requireNonNull(client.getNetworkHandler()).getPlayerListEntry(entry.getKey()) == null);
            if (configScreen && client.currentScreen == null) {
                client.setScreen(new UVGridScreen());
                configScreen = false;
            }
        });
    }
}