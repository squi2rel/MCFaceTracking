package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.FTCuboid;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(at = @At("HEAD"), method = "setModelPose")
    public void update(AbstractClientPlayerEntity player, CallbackInfo ci) {
        FTCuboid.player = player.getUuid();
    }
}
