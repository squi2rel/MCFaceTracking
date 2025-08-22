package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.MCFTClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At("RETURN"), method = "renderWorld")
    private void update(CallbackInfo info) {
        MCFTClient.update();
    }
}
