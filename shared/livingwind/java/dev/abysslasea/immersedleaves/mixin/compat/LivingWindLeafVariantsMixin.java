package dev.abysslasea.immersedleaves.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Leaves variant creation and UV selection to Immersed Leaves on newer Living Wind builds. */
@Pseudo
@Mixin(targets = "com.macatmovie.livingwind.client.LeafTextureVariants", remap = false)
public abstract class LivingWindLeafVariantsMixin {
    @Inject(method = "addSprites(Ljava/util/List;)Ljava/util/List;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void immersedleaves$ownSprites(List<?> originals, CallbackInfoReturnable<List<?>> ci) {
        ci.setReturnValue(originals);
    }

    @Inject(method = "bakeQuads(Ljava/util/List;)Ljava/util/List;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void immersedleaves$ownQuads(List<?> originals, CallbackInfoReturnable<List<?>> ci) {
        ci.setReturnValue(List.of(originals));
    }
}
