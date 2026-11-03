package dev.abysslasea.immersedleaves.mixin;

import dev.abysslasea.immersedleaves.client.LeafTextureVariants;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** The renderer seeds this random source with the block position before querying each face. */
@Mixin(SimpleBakedModel.class)
public abstract class LeafBlockModelMixin {
    @Unique
    private final ConcurrentMap<List<BakedQuad>, List<List<BakedQuad>>> immersedleaves$leafQuads = new ConcurrentHashMap<>();

    @Inject(method = "getQuads", at = @At("RETURN"), cancellable = true)
    private void immersedleaves$chooseLeafVariant(BlockState state, Direction side, RandomSource random,
                                              CallbackInfoReturnable<List<BakedQuad>> ci) {
        if (state == null || !state.is(BlockTags.LEAVES) || ci.getReturnValue().isEmpty()) return;
        List<List<BakedQuad>> variants = immersedleaves$leafQuads.computeIfAbsent(
                ci.getReturnValue(), LeafTextureVariants::bakeQuads);
        if (variants.size() > 1) ci.setReturnValue(variants.get(random.nextInt(variants.size())));
    }
}
