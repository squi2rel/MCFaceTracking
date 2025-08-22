package com.github.squi2rel.mcft.mixin.client;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor("cuboids")
    List<ModelPart.Cuboid> getCuboids();

    @Accessor("cuboids")
    @Mutable
    void setCuboids(List<ModelPart.Cuboid> cuboids);

    @Accessor("children")
    Map<String, ModelPart> getChildren();
}
