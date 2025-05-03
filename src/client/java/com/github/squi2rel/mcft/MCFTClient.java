package com.github.squi2rel.mcft;

import com.github.squi2rel.mcft.services.DNS;
import com.github.squi2rel.mcft.services.HTTP;
import com.github.squi2rel.mcft.services.OSC;
import net.fabricmc.api.ClientModInitializer;

public class MCFTClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        try {
            HTTP.init();
            OSC.init();
            DNS.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}