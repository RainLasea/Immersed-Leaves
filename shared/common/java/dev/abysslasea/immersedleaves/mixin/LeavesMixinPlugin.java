package dev.abysslasea.immersedleaves.mixin;

import dev.abysslasea.immersedleaves.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Selects optional integrations before mod constructors run, without loading target classes. */
public final class LeavesMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".compat.LivingWind")) return Platform.isModLoaded("livingwind");
        if (mixinClassName.endsWith("InteractiveFoliageLeavesMixin")) {
            return Platform.isModLoaded("mc2_interactivefoliage");
        }
        return true;
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
}
