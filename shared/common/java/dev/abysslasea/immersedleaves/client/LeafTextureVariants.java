package dev.abysslasea.immersedleaves.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Builds independently animated atlas slots and preserves model geometry when changing their UVs. */
public final class LeafTextureVariants {
    public static final int COUNT = 16;
    static final String SUFFIX = "_immersedleaves_phase_";
    private static volatile Map<ResourceLocation, TextureAtlasSprite[]> sprites = Map.of();

    private LeafTextureVariants() {}

    public static List<SpriteContents> addSprites(List<SpriteContents> originals) {
        List<SpriteContents> result = new ArrayList<>();
        for (SpriteContents source : originals) {
            String path = source.name().getPath();
            if (!path.startsWith("block/") || !path.contains("leaves")
                    || ((LeafTextureSource) source).immersedleaves$frameCount() != 1
                    || ((LeafTextureSource) source).immersedleaves$originalImage().getWidth() != source.width()
                    || ((LeafTextureSource) source).immersedleaves$originalImage().getHeight() != source.height()) {
                result.add(source);
                continue;
            }
            result.add(source);
            for (int variant = 1; variant < COUNT; variant++) {
                NativeImage image = new NativeImage(source.width(), source.height(), false);
                image.copyFrom(((LeafTextureSource) source).immersedleaves$originalImage());
                result.add(LeafSpriteFactory.create(source, source.name().withSuffix(SUFFIX + variant), image));
            }
        }
        return result;
    }

    public static void bindSprites(Map<ResourceLocation, TextureAtlasSprite> atlas) {
        Map<ResourceLocation, TextureAtlasSprite[]> variants = new HashMap<>();
        for (var entry : atlas.entrySet()) {
            ResourceLocation name = entry.getKey();
            if (!atlas.containsKey(name.withSuffix(SUFFIX + 1))) continue;
            TextureAtlasSprite[] group = new TextureAtlasSprite[COUNT];
            group[0] = entry.getValue();
            for (int variant = 1; variant < COUNT; variant++) {
                group[variant] = atlas.get(name.withSuffix(SUFFIX + variant));
            }
            variants.put(name, group);
        }
        sprites = Map.copyOf(variants);
    }

    /** Cached by the baked model, so chunk rebuilds only select an existing quad list. */
    public static List<List<BakedQuad>> bakeQuads(List<BakedQuad> quads) {
        Map<ResourceLocation, TextureAtlasSprite[]> currentSprites = sprites;
        if (quads.stream().noneMatch(quad -> currentSprites.containsKey(quad.getSprite().contents().name()))) {
            return List.of(quads);
        }
        List<List<BakedQuad>> variants = new ArrayList<>(COUNT);
        variants.add(quads);
        for (int variant = 1; variant < COUNT; variant++) {
            List<BakedQuad> remapped = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                TextureAtlasSprite original = quad.getSprite();
                TextureAtlasSprite[] group = currentSprites.get(original.contents().name());
                if (group == null) {
                    remapped.add(quad);
                    continue;
                }
                TextureAtlasSprite target = group[variant];
                int[] vertices = quad.getVertices().clone();
                int stride = DefaultVertexFormat.BLOCK.getVertexSize() / Integer.BYTES;
                // BLOCK stores position (3 ints), color (1), then the two texture coordinates.
                for (int vertex = 0; vertex < 4; vertex++) {
                    int uv = vertex * stride + 4;
                    float u = (Float.intBitsToFloat(vertices[uv]) - original.getU0())
                            / (original.getU1() - original.getU0());
                    float v = (Float.intBitsToFloat(vertices[uv + 1]) - original.getV0())
                            / (original.getV1() - original.getV0());
                    vertices[uv] = Float.floatToRawIntBits(target.getU0() + u * (target.getU1() - target.getU0()));
                    vertices[uv + 1] = Float.floatToRawIntBits(target.getV0() + v * (target.getV1() - target.getV0()));
                }
                remapped.add(LeafSpriteFactory.copyQuad(quad, vertices, target));
            }
            variants.add(List.copyOf(remapped));
        }
        return List.copyOf(variants);
    }
}
