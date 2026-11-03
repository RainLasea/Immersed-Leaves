package dev.abysslasea.immersedleaves.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Shares leaf-cluster geometry and uploads only mip levels whose sampled pixels changed. */
public final class LeafTextureAnimation implements SpriteTicker {
    private static final Map<ResourceLocation, Source> SOURCES = new HashMap<>();
    private static final Set<LeafTextureAnimation> ANIMATIONS = new LinkedHashSet<>();
    private static float strength;
    private static float gust;
    private static float motionSpeed = 0.2f;
    private static long lastFrameNanos;
    private final ResourceLocation sourceName;
    private final Source source;
    private final LeafTextureMapping mapping;
    private final NativeImage[] images;
    private final int[][] renderedPixels;
    private final boolean[] changedLevels;
    private int atlasX;
    private int atlasY;
    private int atlasTexture = -1;

    public LeafTextureAnimation(NativeImage original, int width, int height, int mipLevel, ResourceLocation name) {
        int variant = 0;
        for (int index = 1; index < LeafTextureVariants.COUNT; index++) {
            String suffix = LeafTextureVariants.SUFFIX + index;
            if (name.getPath().endsWith(suffix)) {
                name = name.withPath(name.getPath().substring(0, name.getPath().length() - suffix.length()));
                variant = index;
                break;
            }
        }
        sourceName = name;
        source = SOURCES.computeIfAbsent(name, key -> new Source(original, width, height, mipLevel, key.hashCode()));
        source.users++;
        mapping = new LeafTextureMapping(source.layout, variant * 36.0 / LeafTextureVariants.COUNT);
        images = new NativeImage[source.pixels.length];
        renderedPixels = new int[images.length][];
        changedLevels = new boolean[images.length];
        for (int level = 0; level < images.length; level++) {
            NativeImage image = new NativeImage(width >> level, height >> level, false);
            images[level] = image;
            int[] pixels = source.pixels[level].clone();
            renderedPixels[level] = pixels;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    image.setPixelRGBA(x, y, pixels[y * image.getWidth() + x]);
                }
            }
        }
        ANIMATIONS.add(this);
    }

    public static void updateWind(float newStrength, float newGust) {
        updateWind(newStrength, newGust, 1.0f);
    }

    public static void updateWind(float newStrength, float newGust, float newMotionSpeed) {
        strength = newStrength;
        gust = newGust;
        motionSpeed = newMotionSpeed;
    }

    public static void renderFrame(long now, boolean paused) {
        double seconds = lastFrameNanos == 0 ? 0.0 : (now - lastFrameNanos) * 1.0e-9;
        lastFrameNanos = now;
        if (ANIMATIONS.isEmpty() || paused) return;
        int previousTexture = -1;
        int boundTexture = -1;
        for (LeafTextureAnimation animation : ANIMATIONS) {
            if (animation.atlasTexture < 0) continue;
            if (!animation.update(seconds)) continue;
            if (previousTexture < 0) {
                previousTexture = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
                boundTexture = previousTexture;
            }
            if (boundTexture != animation.atlasTexture) {
                RenderSystem.bindTexture(animation.atlasTexture);
                boundTexture = animation.atlasTexture;
            }
            for (int level = 0; level < animation.images.length; level++) {
                if (!animation.changedLevels[level]) continue;
                NativeImage image = animation.images[level];
                image.upload(level, animation.atlasX >> level, animation.atlasY >> level, 0, 0,
                        image.getWidth(), image.getHeight(), animation.images.length > 1, false);
            }
        }
        if (previousTexture >= 0 && boundTexture != previousTexture) RenderSystem.bindTexture(previousTexture);
    }

    @Override
    public void tickAndUpload(int x, int y) {
        // The atlas binds itself before native tickers run; capture its slot without a 20 Hz upload.
        atlasX = x;
        atlasY = y;
        if (atlasTexture < 0) atlasTexture = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
    }

    private boolean update(double seconds) {
        mapping.advance(seconds * motionSpeed, strength, gust);
        boolean changed = false;
        for (int level = 0; level < images.length; level++) {
            boolean levelChanged = false;
            NativeImage image = images[level];
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = source.pixels[level];
            int[] rendered = renderedPixels[level];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    int color = pixels[mapping.sourceIndex(x, y, level)];
                    if (rendered[index] == color) continue;
                    rendered[index] = color;
                    image.setPixelRGBA(x, y, color);
                    levelChanged = true;
                }
            }
            changedLevels[level] = levelChanged;
            changed |= levelChanged;
        }
        return changed;
    }

    @Override
    public void close() {
        ANIMATIONS.remove(this);
        for (NativeImage image : images) image.close();
        if (--source.users == 0) SOURCES.remove(sourceName, source);
        if (ANIMATIONS.isEmpty()) lastFrameNanos = 0;
    }

    private static final class Source {
        private final int[][] pixels;
        private final LeafTextureMapping.Layout layout;
        private int users;

        private Source(NativeImage original, int width, int height, int mipLevel, int seed) {
            NativeImage copy = new NativeImage(width, height, false);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) copy.setPixelRGBA(x, y, original.getPixelRGBA(x, y));
            }
            NativeImage[] mipmaps = MipmapGenerator.generateMipLevels(new NativeImage[]{copy}, mipLevel);
            int levels = 0;
            while (levels < mipmaps.length && (width >> levels) > 0 && (height >> levels) > 0) levels++;
            pixels = new int[levels][];
            for (int level = 0; level < mipmaps.length; level++) {
                NativeImage image = mipmaps[level];
                if (level < levels) {
                    pixels[level] = new int[image.getWidth() * image.getHeight()];
                    for (int y = 0; y < image.getHeight(); y++) {
                        for (int x = 0; x < image.getWidth(); x++) {
                            pixels[level][y * image.getWidth() + x] = image.getPixelRGBA(x, y);
                        }
                    }
                }
                image.close();
            }
            layout = new LeafTextureMapping.Layout(width, height, levels, seed);
        }
    }
}
