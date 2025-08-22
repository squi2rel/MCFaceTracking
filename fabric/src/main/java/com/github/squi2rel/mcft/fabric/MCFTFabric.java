package com.github.squi2rel.mcft.fabric;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;

public final class MCFTFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        MCFT.onInitialize();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            MCFTClient.onInitializeClient();
        }
    }
}
