package com.github.squi2rel.mcft.mixin.client;

import net.minecraft.client.model.ModelPart;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.Quad.class)
public interface QuadAccessor {
    @Accessor("vertices")
    ModelPart.Vertex[] getVertices();

    @Accessor("direction")
    Vector3f getDirection();
}
