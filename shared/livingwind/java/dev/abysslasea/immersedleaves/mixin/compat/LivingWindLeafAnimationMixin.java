package dev.abysslasea.immersedleaves.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents duplicate frame uploads when Living Wind includes its own texture animation. */
@Pseudo
@Mixin(targets = "com.macatmovie.livingwind.client.LeafTextureAnimation", remap = false)
public abstract class LivingWindLeafAnimationMixin {
    @Inject(method = "renderFrame(JZ)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void immersedleaves$ownAnimation(CallbackInfo ci) {
        ci.cancel();
    }
}
