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

import java.util.concurrent.CountDownLatch;

@Mod(MCFT.MOD_ID)
public final class MCFTNeoForge {
    public static CountDownLatch latch = new CountDownLatch(1);

    public MCFTNeoForge(IEventBus modEventBus) {
        // Run our common setup.
        MCFT.onInitialize();
        modEventBus.register(MCFTNeoForge.class);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MCFTClient.onInitializeClient();
        latch.countDown();
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK);
        PacketHandlers.register(registrar);
    }
}
