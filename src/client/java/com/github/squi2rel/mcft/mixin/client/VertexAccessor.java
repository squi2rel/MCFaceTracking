package com.github.squi2rel.mcft.mixin.client;

import net.minecraft.client.model.ModelPart;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.Vertex.class)
public interface VertexAccessor {
    @Accessor("pos")
    Vector3f getPos();
}
