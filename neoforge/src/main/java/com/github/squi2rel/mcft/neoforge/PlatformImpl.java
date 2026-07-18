package com.github.squi2rel.mcft.neoforge;

import com.github.squi2rel.mcft.MCFT;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class PlatformImpl {
    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("mcft-server.json");
    }

    public static Path getClientConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve("mcft.json");
    }

    public static String getVersion() {
        return ModList.get().getModContainerById(MCFT.MOD_ID).orElseThrow().getModInfo().getVersion().toString();
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(PlatformImpl.class);
    }

    public static void registerCommand() {
        NeoForge.EVENT_BUS.register(ClientPlatformEvents.class);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity player) {
            MCFT.onPlayerJoin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity player) {
            MCFT.onPlayerLeave(player);
        }
    }
}
