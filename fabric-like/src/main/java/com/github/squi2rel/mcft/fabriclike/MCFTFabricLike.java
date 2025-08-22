package com.github.squi2rel.mcft.fabriclike;

import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class MCFTFabricLike {
    public static void init() {
        // Run our common setup.
        MCFT.onInitialize();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            MCFTClient.onInitializeClient();
        }
    }
}
