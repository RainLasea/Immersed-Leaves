package dev.abysslasea.immersedleaves.client;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;

/** Preserves the source sprite and quad metadata exposed by Fabric 1.20.1. */
public final class LeafSpriteFactory {
    private LeafSpriteFactory() {}
    public static SpriteContents create(SpriteContents source, ResourceLocation name, NativeImage image) {
        FrameSize size = new FrameSize(image.getWidth(), image.getHeight());
        return new SpriteContents(name, size, image, AnimationMetadataSection.EMPTY);
    }
    public static BakedQuad copyQuad(BakedQuad quad, int[] vertices, TextureAtlasSprite target) {
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), target, quad.isShade());
    }
}
