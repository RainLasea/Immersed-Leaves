package dev.abysslasea.immersedleaves.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Leaves and the blocks resting on them stay on the chunk mesh while the leaf texture moves. */
@Pseudo
@Mixin(targets = "net.karto.mc2.mc2_interactivefoliage.WavingWhitelist", remap = false)
public abstract class InteractiveFoliageLeavesMixin {
    @Inject(method = "leavesWave()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void immersedleaves$textureInsteadOfGeometry(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(false);
    }
}
