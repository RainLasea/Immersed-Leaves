package dev.abysslasea.immersedleaves.client;

import dev.abysslasea.immersedleaves.Platform;
import net.minecraft.client.Minecraft;

/** Uses Living Wind when synchronized, otherwise a gentle weather-driven breeze. */
public final class LeafWind {
    private static final boolean LIVING_WIND = Platform.isModLoaded("livingwind");

    private LeafWind() {}

    public static void update() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            LeafTextureAnimation.updateWind(0.0f, 0.0f);
            return;
        }
        float rain = minecraft.level.getRainLevel(1.0f);
        float thunder = minecraft.level.getThunderLevel(1.0f);
        float ambientStrength = 0.2f + rain * 0.65f + thunder * 0.75f;
        float ambientGust = thunder * 0.5f;
        float speed = 0.2f + rain * 0.45f + thunder * 0.35f;
        if (LIVING_WIND && Platform.updateLivingWind(ambientStrength, ambientGust, speed)) return;
        LeafTextureAnimation.updateWind(ambientStrength, ambientGust, speed);
    }
}
