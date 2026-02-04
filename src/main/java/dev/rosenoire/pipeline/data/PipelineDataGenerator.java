package dev.rosenoire.pipeline.data;

import dev.rosenoire.pipeline.common.Pipeline;
import dev.rosenoire.pipeline.common.block.CopperPipeBlock;
import dev.rosenoire.pipeline.common.index.ModBlocks;
import dev.rosenoire.pipeline.common.index.ModItemGroups;
import dev.rosenoire.pipeline.common.index.ModItems;
import net.collectively.geode.datagen.GeodeDataGeneration;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.AxisRotation;

import java.util.concurrent.CompletableFuture;

public class PipelineDataGenerator implements DataGeneratorEntrypoint {
    public static class Generator extends GeodeDataGeneration {
        public Generator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(dataOutput, registriesFuture);
        }

        @Override
        public String getModId() {
            return Pipeline.MOD_ID;
        }

        @Override
        protected void generate() {
            Pipeline.log.info("PipelineDataGenerator.Generator#generate");

            addItem(ModItems.COPPER_PIPE).autoTranslate();
            addItemGroup(ModItemGroups.PIPELINE).autoTranslate();

            addBlock(ModBlocks.COPPER_PIPE)
                    .autoTranslate()
                    .multipartBlockstate(x -> x
                            .with("block/copper_pipe")
                            .with(CopperPipeBlock.NORTH, true, "block/copper_pipe_straight")
                            .with(CopperPipeBlock.SOUTH, true, "block/copper_pipe_straight_south")
                            .with(
                                    CopperPipeBlock.EAST, true,
                                    model -> model.withRotationY(AxisRotation.R90),
                                    "block/copper_pipe_straight"
                            )
                            .with(
                                    CopperPipeBlock.WEST, true,
                                    model -> model.withRotationY(AxisRotation.R90),
                                    "block/copper_pipe_straight_south"
                            )
                            .with(
                                    CopperPipeBlock.UP, true,
                                    model -> model.withRotationY(AxisRotation.R90),
                                    "block/copper_pipe_straight_up"
                            )
                            .with(
                                    CopperPipeBlock.DOWN, true,
                                    model -> model
                                            .withRotationX(AxisRotation.R90),
                                    "block/copper_pipe_straight_down"
                            )
                    );
        }
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(Generator::new);
    }
}
