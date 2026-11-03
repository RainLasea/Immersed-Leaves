package dev.abysslasea.immersedleaves.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Reads the existing client state without making Living Wind a required dependency. */
@Pseudo
@Mixin(targets = "com.macatmovie.livingwind.client.ClientWindState", remap = false)
public interface LivingWindAccess {
    @Invoker(value = "hasServerState", remap = false)
    static boolean immersedleaves$hasServerState() { throw new AssertionError(); }

    @Invoker(value = "altitudeStrength", remap = false)
    static float immersedleaves$altitudeStrength() { throw new AssertionError(); }

    @Invoker(value = "gustStrength", remap = false)
    static float immersedleaves$gustStrength() { throw new AssertionError(); }
}
