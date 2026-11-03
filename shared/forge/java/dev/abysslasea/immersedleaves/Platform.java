package dev.abysslasea.immersedleaves;
import net.minecraftforge.fml.loading.FMLLoader;
import dev.abysslasea.immersedleaves.client.LivingWindCompat;
/** Uses the loader discovery list, available before mod construction. */
public final class Platform {
    private Platform() {}
    public static boolean isModLoaded(String id) { return FMLLoader.getLoadingModList().getModFileById(id) != null; }
    public static boolean updateLivingWind(float ambientStrength, float ambientGust, float speed) {
        return LivingWindCompat.update(ambientStrength, ambientGust, speed);
    }
}
