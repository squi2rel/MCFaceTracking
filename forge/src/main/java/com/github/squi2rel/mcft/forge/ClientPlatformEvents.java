package com.github.squi2rel.mcft.forge;

import com.github.squi2rel.mcft.MCFTClient;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientPlatformEvents {
    @SubscribeEvent
    public static void onCommandRegister(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<ServerCommandSource>literal("mcft").executes(s -> {
            MCFTClient.configScreen = true;
            return 1;
        }));
    }
}
