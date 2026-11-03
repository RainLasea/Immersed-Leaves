package dev.abysslasea.immersedleaves.client;

import com.mojang.blaze3d.platform.NativeImage;

/** Exposes the native frame count before atlas tickers are created. */
public interface LeafTextureSource {
    int immersedleaves$frameCount();
    NativeImage immersedleaves$originalImage();
}
