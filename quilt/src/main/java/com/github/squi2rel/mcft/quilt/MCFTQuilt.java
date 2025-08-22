package com.github.squi2rel.mcft.quilt;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

import com.github.squi2rel.mcft.fabriclike.MCFTFabricLike;

public final class MCFTQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        // Run the Fabric-like setup.
        MCFTFabricLike.init();
    }
}
