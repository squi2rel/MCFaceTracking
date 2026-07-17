package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.FTCuboid;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = ModelPart.class, priority = 900)
public class ModelPartRenderMixin {
    @Unique
    private static final Class<?> mcft$sodiumModelPartData = mcft$findSodiumModelPartData();

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mcft$render(
            MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
            float red, float green, float blue, float alpha, CallbackInfo ci
    ) {
        ModelPart part = (ModelPart) (Object) this;
        if (mcft$sodiumModelPartData == null || !mcft$sodiumModelPartData.isInstance(part)) return;
        ModelPartAccessor accessor = (ModelPartAccessor) (Object) part;
        List<ModelPart.Cuboid> cuboids = accessor.getCuboids();
        if (!mcft$containsCustomCuboid(cuboids)) return;
        mcft$renderVanilla(part, accessor, cuboids, matrices, vertices, light, overlay, red, green, blue, alpha);
        ci.cancel();
    }

    @Unique
    private static Class<?> mcft$findSodiumModelPartData() {
        try {
            return Class.forName(
                    "me.jellysquid.mods.sodium.client.render.immediate.model.ModelPartData",
                    false,
                    ModelPart.class.getClassLoader()
            );
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    @Unique
    private static boolean mcft$containsCustomCuboid(List<ModelPart.Cuboid> cuboids) {
        for (ModelPart.Cuboid cuboid : cuboids) {
            if (cuboid instanceof FTCuboid) return true;
        }
        return false;
    }

    @Unique
    private static void mcft$renderVanilla(
            ModelPart part, ModelPartAccessor accessor, List<ModelPart.Cuboid> cuboids,
            MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
            float red, float green, float blue, float alpha
    ) {
        if (!accessor.getVisible()) return;
        Map<String, ModelPart> children = accessor.getChildren();
        if (cuboids.isEmpty() && children.isEmpty()) return;

        matrices.push();
        part.rotate(matrices);
        if (!accessor.getHidden()) {
            MatrixStack.Entry entry = matrices.peek();
            for (ModelPart.Cuboid cuboid : cuboids) {
                cuboid.renderCuboid(entry, vertices, light, overlay, red, green, blue, alpha);
            }
        }
        for (ModelPart child : children.values()) {
            child.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
        matrices.pop();
    }
}
