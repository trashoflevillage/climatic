package io.github.trashoflevillage.climatic.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiNoiseUtil.MultiNoiseSampler.class)
public class MultiNoiseSamplerMixin {
    @Final @Shadow private DensityFunction temperature;
    @Final @Shadow private DensityFunction humidity;
    @Final @Shadow private DensityFunction continentalness;
    @Final @Shadow private DensityFunction erosion;
    @Final @Shadow private DensityFunction depth;
    @Final @Shadow private DensityFunction weirdness;

    @ModifyReturnValue(method = "sample", at = @At("TAIL"))
    private MultiNoiseUtil.NoiseValuePoint modifyTemperatureValue(MultiNoiseUtil.NoiseValuePoint original, int x, int y, int z) {
        int i = BiomeCoords.toBlock(x);
        int j = BiomeCoords.toBlock(y);
        int k = BiomeCoords.toBlock(z);
        DensityFunction.UnblendedNoisePos unblendedNoisePos = new DensityFunction.UnblendedNoisePos(i, j, k);

        // The temperature that the vanilla game would use at this location.
        float originalTemp = (float)this.temperature.sample(unblendedNoisePos);

        // How gradually the temperature changes in the north/south directions.
        float deltaTemp = 1000f;

//        float tempMod = (float)(2 * (1/(1+Math.pow(Math.E, (-z / deltaTemp)))));
        float tempMod = z / deltaTemp;

        float newTemp = ((2 * originalTemp) + tempMod)/3;

        if (2 - newTemp <= 0.1) newTemp = 2f;

        return MultiNoiseUtil.createNoiseValuePoint(
                newTemp,
                (float)this.humidity.sample(unblendedNoisePos),
                (float)this.continentalness.sample(unblendedNoisePos),
                (float)this.erosion.sample(unblendedNoisePos),
                (float)this.depth.sample(unblendedNoisePos),
                (float)this.weirdness.sample(unblendedNoisePos)
        );
    }
}
