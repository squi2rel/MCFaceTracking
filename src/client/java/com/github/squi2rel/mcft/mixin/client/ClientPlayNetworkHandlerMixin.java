package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.MCFTClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "clearWorld")
    public void clearWorld(CallbackInfo ci) {
        MCFTClient.connected = false;
    }
}
