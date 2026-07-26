package com.github.squi2rel.mcft.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MCFTMixinPlugin implements IMixinConfigPlugin {
    private static final String MODEL_PART_RENDER_MIXIN = "com.github.squi2rel.mcft.mixin.client.ModelPartRenderMixin";
    private static final String[] SODIUM_MODEL_PART_MIXINS = {
            "net/caffeinemc/mods/sodium/mixin/features/render/entity/ModelPartMixin.class",
            "me/jellysquid/mods/sodium/mixin/features/render/entity/ModelPartMixin.class"
    };
    private boolean sodiumEntityRendererAvailable;

    @Override
    public void onLoad(String mixinPackage) {
        ClassLoader loader = MCFTMixinPlugin.class.getClassLoader();
        for (String resource : SODIUM_MODEL_PART_MIXINS) {
            if (loader.getResource(resource) != null) {
                sodiumEntityRendererAvailable = true;
                break;
            }
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !MODEL_PART_RENDER_MIXIN.equals(mixinClassName) || sodiumEntityRendererAvailable;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
