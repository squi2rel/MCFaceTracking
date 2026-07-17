package com.github.squi2rel.mcft.neoforge;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(MCFT.MOD_ID)
public final class MCFTNeoForge {
    public MCFTNeoForge(IEventBus modEventBus) {
        // Run our common setup.
        MCFT.onInitialize();
        modEventBus.register(MCFTNeoForge.class);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MCFTClient.onInitializeClient();
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK);
        PacketHandlers.register(registrar);
    }
}
