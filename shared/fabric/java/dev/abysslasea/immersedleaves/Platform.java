package dev.abysslasea.immersedleaves;
import net.fabricmc.loader.api.FabricLoader;
/** Uses the loader discovery list, available before mod construction. */
public final class Platform {
    private Platform() {}
    public static boolean isModLoaded(String id) { return FabricLoader.getInstance().isModLoaded(id); }
    public static boolean updateLivingWind(float ambientStrength, float ambientGust, float speed) { return false; }
}
