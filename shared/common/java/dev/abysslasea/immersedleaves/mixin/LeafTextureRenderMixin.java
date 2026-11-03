package dev.abysslasea.immersedleaves.mixin;

import dev.abysslasea.immersedleaves.client.LeafTextureAnimation;
import dev.abysslasea.immersedleaves.client.LeafWind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Updates leaf atlas pixels before each rendered frame on both supported versions. */
@Mixin(GameRenderer.class)
public abstract class LeafTextureRenderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void immersedleaves$animateLeaves(CallbackInfo ci) {
        LeafWind.update();
        LeafTextureAnimation.renderFrame(System.nanoTime(), Minecraft.getInstance().isPaused());
    }
}
