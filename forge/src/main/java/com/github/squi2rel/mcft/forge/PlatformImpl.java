package com.github.squi2rel.mcft.forge;

import com.github.squi2rel.mcft.MCFT;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

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
        MinecraftForge.EVENT_BUS.register(PlatformImpl.class);
    }

    public static void registerCommand() {
        MinecraftForge.EVENT_BUS.register(ClientPlatformEvents.class);
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
