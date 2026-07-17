package com.github.squi2rel.mcft.neoforge;

import com.github.squi2rel.mcft.MCFTClient;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public class ClientPlatformEvents {
    @SubscribeEvent
    public static void onCommandRegister(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<ServerCommandSource>literal("mcft").executes(s -> {
            MCFTClient.configScreen = true;
            return 1;
        }));
    }
}
