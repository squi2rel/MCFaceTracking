package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.ConfigPayload;
import com.github.squi2rel.mcft.network.TrackingParamsPayload;
import com.github.squi2rel.mcft.network.TrackingUpdatePayload;
import com.github.squi2rel.mcft.services.HTTP;
import com.github.squi2rel.mcft.services.OSC;
import com.github.squi2rel.mcft.ui.UVGridScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static com.github.squi2rel.mcft.FTModel.model;

@SuppressWarnings("resource")
public class MCFTClient implements ClientModInitializer {
    private static long lastSync = System.currentTimeMillis();
    public static int fps = -1;
    private static boolean configScreen = false;
    public static boolean connected = false;
    public static HashMap<UUID, FTModel> uuidToModel = new HashMap<>();
    public static Config config;
    public static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mcft.json");

	@Override
	public void onInitializeClient() {
        config = MCFT.loadConfig(Config.class, configPath);
        model = config.model;
        model.eyeL.set(config.eyeOffsetXL, config.eyeOffsetYL);
        model.eyeR.set(config.eyeOffsetXR, config.eyeOffsetYR);
        try {
            HTTP.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ClientPlayNetworking.registerGlobalReceiver(TrackingParamsPayload.ID, (p, context) -> context.client().execute(() -> uuidToModel.put(p.player(), new FTModel(p.eyeR(), p.eyeL(), p.mouth(), p.flat()))));

        ClientPlayNetworking.registerGlobalReceiver(TrackingUpdatePayload.ID, (p, context) -> context.client().execute(() -> {
            FTModel model = uuidToModel.get(p.player());
            if (model == null || model == FTModel.model) return;
            model.readSync(p.data());
        }));

        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (p, context) -> context.client().execute(() -> {
            if (!checkVersion(p.version())) {
                Objects.requireNonNull(context.client().player).sendMessage(Text.of("服务器MCFT版本和本地版本不匹配! 本地版本为" + MCFT.version + ", 服务器版本为" + p.version()), false);
                return;
            }
            fps = p.fps();
            if (!connected) MCFT.LOGGER.info("检测到服务端MCFT");
            connected = true;
            FTClient.uploadParams(model);
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

        AutoBlink.init();
    }

    private static boolean checkVersion(String v) {
        String[] p1 = StringUtils.split(v, '.');
        String[] p2 = StringUtils.split(MCFT.version, '.');
        if (p1.length < 2 || p2.length < 2) return false;
        return p1[0].equals(p2[0]) && p1[1].equals(p2[1]);
    }
}