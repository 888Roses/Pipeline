package dev.rosenoire.pipeline.data;

import dev.rosenoire.pipeline.common.Pipeline;
import net.collectively.geode.datagen.GeodeDataGeneration;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class PipelineDataGenerator implements DataGeneratorEntrypoint {
    public static class Generator extends GeodeDataGeneration {
        public Generator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(dataOutput, registriesFuture);
        }

        @Override
        protected void generate() {
            Pipeline.log.info("PipelineDataGenerator.Generator#generate");
        }
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(Generator::new);
    }
}
