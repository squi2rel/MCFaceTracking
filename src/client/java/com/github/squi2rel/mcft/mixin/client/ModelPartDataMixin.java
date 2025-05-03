package com.github.squi2rel.mcft.mixin.client;

import com.github.squi2rel.mcft.ModelPartDataExtension;
import com.github.squi2rel.mcft.FTCuboid;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ModelPartData.class)
public class ModelPartDataMixin implements ModelPartDataExtension {
    @Unique
    private boolean isPlayerModel = false;

    @Override
    public void MCFT$isPlayerModel(boolean is) {
        isPlayerModel = is;
    }

    @Inject(at = @At("RETURN"), method = "createPart")
    public void createPart(int textureWidth, int textureHeight, CallbackInfoReturnable<ModelPart> cir) {
        if (!isPlayerModel) return;
        ModelPartAccessor accessor = (ModelPartAccessor) (Object) cir.getReturnValue().getChild(EntityModelPartNames.HEAD);
        if (accessor == null || accessor.getChildren().get(EntityModelPartNames.LEFT_EAR) != null) return; // piglin
        accessor.setCuboids(List.of(FTCuboid.newInstance(accessor.getCuboids().getFirst())));
    }
}
