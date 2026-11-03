package dev.abysslasea.immersedleaves.client;

import dev.abysslasea.immersedleaves.mixin.compat.LivingWindAccess;

/** Keeps occasional idle breezes at zero wind while stronger wind and weather drive larger motion. */
public final class LivingWindCompat {
    private LivingWindCompat() {}

    public static boolean update(float ambientStrength, float ambientGust, float speed) {
        if (!LivingWindAccess.immersedleaves$hasServerState()) return false;
        float wind = LivingWindAccess.immersedleaves$altitudeStrength();
        LeafTextureAnimation.updateWind(Math.max(ambientStrength, wind),
                Math.max(ambientGust, LivingWindAccess.immersedleaves$gustStrength()),
                Math.max(speed, 0.2f + Math.min(1.0f, wind) * 0.8f));
        return true;
    }
}
