package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.ui.AvatarGridScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(
            method = "drawEntity(Lnet/minecraft/client/gui/DrawContext;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/util/math/MatrixStack.multiplyPositionMatrix(Lorg/joml/Matrix4f;)V",
                    shift =  At.Shift.AFTER
            )
    )
    private static void inject(DrawContext context, int x, int y, int size, Quaternionf quaternionf, Quaternionf quaternionf2, LivingEntity entity, CallbackInfo ci) {
        Vector3f translate = AvatarGridScreen.translate;
        context.getMatrices().translate(translate.x, translate.y, translate.z);
    }
}
