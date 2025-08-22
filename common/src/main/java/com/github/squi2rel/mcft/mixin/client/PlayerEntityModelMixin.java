package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.ext.ModelPartDataExtension;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {
    @Inject(at = @At("RETURN"), method = "getTexturedModelData")
    private static void hook(Dilation dilation, boolean slim, CallbackInfoReturnable<ModelData> cir) {
        ((ModelPartDataExtension) cir.getReturnValue().getRoot()).MCFT$isPlayerModel(true);
    }
}
