package dev.abysslasea.immersedleaves.mixin;

import dev.abysslasea.immersedleaves.client.LeafTextureAnimation;
import dev.abysslasea.immersedleaves.client.LeafTextureSource;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Claims only vanilla's static-texture branch, before optional mods intercept its return. */
@Mixin(SpriteContents.class)
public abstract class LeafTextureAnimationMixin implements LeafTextureSource {
    @Shadow @Final private NativeImage originalImage;
    @Shadow private NativeImage[] byMipLevel;

    @Override
    @Invoker("getFrameCount")
    public abstract int immersedleaves$frameCount();

    @Override
    @Accessor("originalImage")
    public abstract NativeImage immersedleaves$originalImage();

    @Inject(method = "createTicker", at = @At(value = "CONSTANT", args = "nullValue=true"), cancellable = true)
    private void immersedleaves$leafTexture(CallbackInfoReturnable<SpriteTicker> ci) {
        SpriteContents sprite = (SpriteContents) (Object) this;
        String path = sprite.name().getPath();
        if (!path.startsWith("block/") || !path.contains("leaves")) return;
        ci.setReturnValue(new LeafTextureAnimation(originalImage, sprite.width(), sprite.height(),
                byMipLevel.length - 1, sprite.name()));
    }
}
