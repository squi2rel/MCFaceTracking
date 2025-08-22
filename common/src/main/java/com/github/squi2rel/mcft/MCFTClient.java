package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.network.ConfigPayload;
import com.github.squi2rel.mcft.network.TrackingParamsPayload;
import com.github.squi2rel.mcft.network.TrackingUpdatePayload;
import com.github.squi2rel.mcft.services.HTTP;
import com.github.squi2rel.mcft.services.OSC;
import com.github.squi2rel.mcft.ui.UVGridScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static com.github.squi2rel.mcft.FTModel.model;

@Environment(EnvType.CLIENT)
public class MCFTClient {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    private static long lastSync = System.currentTimeMillis();
    public static int fps = -1;
    public static boolean configScreen = false;
    public static boolean connected = false;
    public static HashMap<UUID, FTModel> uuidToModel = new HashMap<>();
    public static Config config;
    public static final Path configPath = Platform.getClientConfigPath();

	public static void onInitializeClient() {
        config = MCFT.loadConfig(Config.class, configPath);
        model = config.model;
        model.eyeL.rawPos.set(config.eyeOffsetXL, config.eyeOffsetYL);
        model.eyeR.rawPos.set(config.eyeOffsetXR, config.eyeOffsetYR);
        try {
            HTTP.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ClientPacketHandler.registerS2C(TrackingParamsPayload.class, TrackingParamsPayload.ID, TrackingParamsPayload.CODEC, p -> MC.execute(() -> uuidToModel.put(p.player(), new FTModel(p.eyeR(), p.eyeL(), p.mouth(), p.flat()))));

        ClientPacketHandler.registerS2C(TrackingUpdatePayload.class, TrackingUpdatePayload.ID, TrackingUpdatePayload.CODEC, p -> MC.execute(() -> {
            FTModel model = uuidToModel.get(p.player());
            if (model == null || model == FTModel.model) return;
            model.readSync(p.data());
        }));

        ClientPacketHandler.registerS2C(ConfigPayload.class, ConfigPayload.ID, ConfigPayload.CODEC, p -> MC.execute(() -> {
            if (!checkVersion(p.version())) {
                Objects.requireNonNull(MC.player).sendMessage(Text.of("服务器MCFT版本和本地版本不匹配! 本地版本为" + MCFT.version + ", 服务器版本为" + p.version()), false);
                return;
            }
            fps = p.fps();
            if (!connected) MCFT.LOGGER.info("检测到服务端MCFT");
            connected = true;
            FTClient.uploadParams(model);
        }));

        Platform.registerCommand();
    }

    public static void update() {
        if (MC.player == null || MC.world == null) return;
        AutoBlink.update();
        if (model.active() && System.currentTimeMillis() - lastSync > 1000 / fps) {
            FTClient.writeSync(model);
            lastSync = System.currentTimeMillis();
        }
        model.lastReceived = OSC.lastReceived;
        uuidToModel.put(MC.player.getUuid(), model);
        uuidToModel.entrySet().removeIf(entry -> Objects.requireNonNull(MC.getNetworkHandler()).getPlayerListEntry(entry.getKey()) == null);
        if (configScreen && MC.currentScreen == null) {
            MC.setScreen(new UVGridScreen());
            configScreen = false;
        }
    }

    private static boolean checkVersion(String v) {
        String[] p1 = StringUtils.split(v, '.');
        String[] p2 = StringUtils.split(MCFT.version, '.');
        if (p1.length < 2 || p2.length < 2) return false;
        return p1[0].equals(p2[0]) && p1[1].equals(p2[1]);
    }
}