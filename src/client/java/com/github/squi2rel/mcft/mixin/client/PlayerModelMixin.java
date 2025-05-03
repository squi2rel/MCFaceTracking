package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.ModelPartDataExtension;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityModel.class)
public class PlayerModelMixin {
    @Inject(at = @At("RETURN"), method = "getTexturedModelData")
    private static void hook(Dilation dilation, boolean slim, CallbackInfoReturnable<ModelData> cir) {
        ((ModelPartDataExtension) cir.getReturnValue().getRoot()).MCFT$isPlayerModel(true);
    }
}
