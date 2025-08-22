package com.github.squi2rel.mcft.neoforge;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
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
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<ServerCommandSource>literal("mcft").executes(s -> {
            MCFTClient.configScreen = true;
            return 1;
        }));
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
