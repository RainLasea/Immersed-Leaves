package dev.abysslasea.immersedleaves.mixin;

import dev.abysslasea.immersedleaves.client.LeafTextureVariants;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Leaf variants participate in normal atlas stitching, mipmaps, and texture cleanup. */
@Mixin(SpriteLoader.class)
public abstract class LeafSpriteLoaderMixin {
    @Shadow @Final private ResourceLocation location;

    @ModifyVariable(method = "stitch", at = @At("HEAD"), argsOnly = true)
    private List<SpriteContents> immersedleaves$addLeafVariants(List<SpriteContents> sprites) {
        return TextureAtlas.LOCATION_BLOCKS.equals(location) ? LeafTextureVariants.addSprites(sprites) : sprites;
    }

    @Inject(method = "stitch", at = @At("RETURN"))
    private void immersedleaves$bindLeafVariants(CallbackInfoReturnable<SpriteLoader.Preparations> ci) {
        if (TextureAtlas.LOCATION_BLOCKS.equals(location)) {
            LeafTextureVariants.bindSprites(ci.getReturnValue().regions());
        }
    }
}
