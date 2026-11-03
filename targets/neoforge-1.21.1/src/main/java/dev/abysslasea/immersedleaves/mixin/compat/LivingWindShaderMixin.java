package dev.abysslasea.immersedleaves.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.charset.StandardCharsets;

/** Removes only the Euphoria leaf vertex-wave call from Living Wind's generated patch. */
@Pseudo
@Mixin(targets = "com.macatmovie.livingwind.client.shader.LivingWindShaderAutoPatcher", remap = false)
public abstract class LivingWindShaderMixin {
    @Inject(method = "patchSource([B)[B", at = @At("RETURN"), cancellable = true, remap = false)
    private static void immersedleaves$textureInsteadOfVertices(byte[] original, CallbackInfoReturnable<byte[]> ci) {
        byte[] patched = ci.getReturnValue();
        String source = new String(patched == null ? original : patched, StandardCharsets.UTF_8);
        String call = "DoWave_Leaves(playerPos.xyz, worldPos, 1.0);";
        if (source.contains("MCWIND_INTERNAL") && source.contains(call)) {
            ci.setReturnValue(source.replace(call, "; /* Immersed Leaves: atlas animation */")
                    .getBytes(StandardCharsets.UTF_8));
        }
    }
}
