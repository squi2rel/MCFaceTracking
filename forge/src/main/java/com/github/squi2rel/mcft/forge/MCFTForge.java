package com.github.squi2rel.mcft.forge;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MCFT.MOD_ID)
public final class MCFTForge {
    public MCFTForge(FMLJavaModLoadingContext context) {
        // Run our common setup.
        MCFT.onInitialize();
        context.getModEventBus().register(MCFTForge.class);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MCFTClient.onInitializeClient();
    }
}
