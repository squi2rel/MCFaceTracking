package com.github.squi2rel.mcft.quilt;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class PlatformImpl {
    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("mcft-server.json");
    }

    public static Path getClientConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("mcft.json");
    }

    public static String getVersion() {
        return FabricLoader.getInstance().getModContainer(MCFT.MOD_ID).orElseThrow().getMetadata().getVersion().toString();
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((h, s, c) -> MCFT.onPlayerJoin(h.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> MCFT.onPlayerLeave(h.getPlayer()));
    }

    public static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((d, r) -> d.register(ClientCommandManager.literal("mcft").executes(s -> {
            MCFTClient.configScreen = true;
            return 1;
        })));
    }
}
