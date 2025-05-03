package com.github.squi2rel.mcft.mixin.client;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.Cuboid.class)
public interface CuboidAccessor {
    @Accessor("minX")
    float getMinX();

    @Accessor("minY")
    float getMinY();

    @Accessor("minZ")
    float getMinZ();

    @Accessor("minX")
    @Mutable
    void setMinX(float x);

    @Accessor("minY")
    @Mutable
    void setMinY(float y);

    @Accessor("minZ")
    @Mutable
    void setMinZ(float z);

    @Accessor("sides")
    ModelPart.Quad[] getSides();

    @Accessor("sides")
    @Mutable
    void setSides(ModelPart.Quad[] sides);
}
