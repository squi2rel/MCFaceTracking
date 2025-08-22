package com.github.squi2rel.mcft.fabric;

import net.fabricmc.api.ModInitializer;

import com.github.squi2rel.mcft.fabriclike.MCFTFabricLike;

public final class MCFTFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run the Fabric-like setup.
        MCFTFabricLike.init();
    }
}
